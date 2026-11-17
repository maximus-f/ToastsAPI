#!/usr/bin/env python3
"""Run disposable local Minecraft servers and assert toast packets with a bot."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import shutil
import signal
import socket
import subprocess
import sys
import time
import urllib.request
import uuid
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
CACHE = ROOT / '.test-tools'
LOCAL = CACHE / 'server-tests-local.json'
AGENT = 'ToastsAPI-tests/1.0 (https://github.com/maximus-f/ToastsAPI)'
SMOKE = ['1.19.4', '1.20.6', '1.21.11']
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
ACTIVE = None


def fetch(url):
    request = urllib.request.Request(url, headers={'User-Agent': AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def download(url, destination, sha256):
    if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() == sha256:
        return
    print(f'Downloading {destination.name}', flush=True)
    content = fetch(url)
    if hashlib.sha256(content).hexdigest() != sha256:
        raise RuntimeError(f'Checksum mismatch: {url}')
    destination.parent.mkdir(parents=True, exist_ok=True)
    temporary = destination.with_suffix('.download')
    temporary.write_bytes(content)
    temporary.replace(destination)


def matrix():
    modules = ET.parse(ROOT / 'NMS/pom.xml').findall('m:modules/m:module', NS)
    versions = []
    for module in modules:
        value = ET.parse(ROOT / 'NMS' / module.text / 'pom.xml').findtext('m:properties/m:mc-version', namespaces=NS)
        if value:
            versions.append(value.split('-R')[0])
    return versions


def node_runtime():
    """Use local Node or install a checksum-verified, pinned runtime (no global install)."""
    node = CACHE / 'node/bin/node'
    if node.exists():
        return node
    installed = shutil.which('node')
    if installed:
        major = int(subprocess.check_output([installed, '--version'], text=True).strip().split('.')[0][1:])
        if major >= 22:
            return Path(installed)
    system = {'Darwin': 'darwin', 'Linux': 'linux'}.get(platform.system())
    arch = {'arm64': 'arm64', 'aarch64': 'arm64', 'x86_64': 'x64'}.get(platform.machine())
    if not system or not arch:
        raise RuntimeError('Install Node.js 22+ and npm on PATH for this platform.')
    name = f'node-v22.14.0-{system}-{arch}.tar.gz'
    base = 'https://nodejs.org/dist/v22.14.0/'
    checksums = fetch(base + 'SHASUMS256.txt').decode().splitlines()
    digest = next(line.split()[0] for line in checksums if line.split()[-1] == name)
    archive = CACHE / name
    download(base + name, archive, digest)
    node.parent.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(['tar', '-xzf', str(archive), '-C', str(node.parent.parent), '--strip-components=1'], check=True)
    return node


def prepare_bot(node):
    env = dict(os.environ, PATH=str(node.parent) + os.pathsep + os.environ.get('PATH', ''))
    npm = node.parent / 'npm'
    command = str(npm) if npm.exists() else shutil.which('npm')
    if not command:
        raise RuntimeError('npm is required alongside Node.js.')
    subprocess.run([command, 'ci', '--ignore-scripts', '--no-audit', '--no-fund'], cwd=HERE, env=env, check=True)


def required_java(version):
    if version.startswith('26.'):
        return 25
    minor = int(version.split('.')[1])
    if minor <= 16:
        return 8
    if minor == 17:
        return 16
    if minor <= 19 or version in ('1.20.1', '1.20.2', '1.20.4'):
        return 17
    return 21


def java_for(version, config, current_home):
    major = required_java(version)
    home = os.environ.get(f'JAVA_{major}_HOME') or config.get('javaHomes', {}).get(str(major))
    if not home and major == 25:
        home = current_home
    if not home:
        raise RuntimeError(f'Java {major} needed for {version}: set JAVA_{major}_HOME or javaHomes.{major} in {LOCAL}')
    java = Path(home).expanduser() / 'bin/java'
    if not java.is_file():
        raise RuntimeError(f'Java executable is missing: {java}')
    return java


def paper_jar(version):
    """Pin the selected build in the local cache; subsequent runs reuse it."""
    directory = CACHE / 'servers/paper' / version
    metadata = directory / 'build.json'
    if metadata.exists():
        build = json.loads(metadata.read_text())
    else:
        builds = json.loads(fetch(f'https://fill.papermc.io/v3/projects/paper/versions/{version}/builds'))
        if not builds:
            raise RuntimeError(f'No Paper builds published for {version}')
        stable = [b for b in builds if b['channel'] == 'STABLE']
        build = max(stable or builds, key=lambda b: b['id'])
    if build['channel'] != 'STABLE':
        print(f'Paper {version}: using {build["channel"]} build {build["id"]} for this disposable test server', flush=True)
    artifact = build['downloads']['server:default']
    jar = directory / artifact['name']
    download(artifact['url'], jar, artifact['checksums']['sha256'])
    metadata.write_text(json.dumps(build, indent=2) + '\n')
    return jar, build['id'], build['channel']


def free_port():
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        return sock.getsockname()[1]


def stop_server(process):
    if process is None:
        return
    if process.poll() is not None:
        process.stdin.close()
        return
    try:
        process.stdin.write('stop\n')
        process.stdin.flush()
        process.wait(timeout=25)
    except (BrokenPipeError, subprocess.TimeoutExpired):
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
    finally:
        process.stdin.close()


def interrupt(signum, frame):
    raise KeyboardInterrupt()


def run_one(version, args, config, node, directory):
    global ACTIVE
    result = {'version': version, 'server': 'custom' if args.server_dir else 'paper', 'status': 'FAIL'}
    start = time.monotonic()
    directory.mkdir(parents=True)
    result['directory'] = str(directory)
    try:
        supported = subprocess.run([str(node), str(HERE / 'bot.cjs'), '--supports', version], capture_output=True, text=True)
        if supported.returncode:
            result.update(status='UNSUPPORTED', error=f'Bot has no exact protocol support for {version}; no server was started.')
            return result
        java = java_for(version, config, args.java_home)
        if args.server_dir:
            jar = args.server_dir.resolve() / f'{version}.jar'
            if not jar.is_file():
                raise RuntimeError(f'Missing executable server JAR: {jar}')
        else:
            jar, result['build'], result['channel'] = paper_jar(version)
        result['serverSha256'] = hashlib.sha256(jar.read_bytes()).hexdigest()
        port = free_port()
        result['port'] = port
        (directory / 'plugins').mkdir()
        shutil.copy2(args.plugin, directory / 'plugins/toastapi-test-plugin.jar')
        # Written only after explicit acceptance in local settings or --accept-eula.
        (directory / 'eula.txt').write_text('eula=true\n')
        (directory / 'server.properties').write_text(
            f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nenforce-secure-profile=false\n'
            'enable-query=false\nenable-rcon=false\nspawn-protection=0\nmax-players=2\n'
            'view-distance=2\nsimulation-distance=2\nlevel-type=default\ngenerate-structures=false\nallow-nether=false\n'
            'spawn-monsters=false\nspawn-animals=false\nspawn-npcs=false\nnetwork-compression-threshold=-1\n')
        (directory / 'bukkit.yml').write_text('settings:\n  allow-end: false\n')
        bot_uuid = str(uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:ToastTestBot').digest(), version=3))
        (directory / 'ops.json').write_text(json.dumps([{'uuid': bot_uuid, 'name': 'ToastTestBot',
                                                      'level': 4, 'bypassesPlayerLimit': True}]))
        log = directory / 'server.log'
        with log.open('w') as stream:
            ACTIVE = subprocess.Popen([str(java), '-Xms256M', '-Xmx1536M', '-jar', str(jar), 'nogui'],
                                      cwd=directory, stdin=subprocess.PIPE, stdout=stream, stderr=subprocess.STDOUT, text=True)
            deadline = time.monotonic() + args.startup_timeout
            while True:
                content = log.read_text(errors='replace')
                if ACTIVE.poll() is not None:
                    raise RuntimeError(f'Server exited with code {ACTIVE.returncode}; see {log}')
                if re.search(r'Done \([\d.,]+s\)!', content):
                    break
                if time.monotonic() > deadline:
                    raise RuntimeError(f'Server startup timed out; see {log}')
                time.sleep(0.5)
            if 'ToastAPI Plugin enabled!' not in content:
                raise RuntimeError('ToastsAPITest did not enable successfully')
            with (directory / 'bot.log').open('w') as botlog:
                bot = subprocess.run([str(node), str(HERE / 'bot.cjs'), version, str(port), str(directory / 'bot.json')],
                                     stdout=botlog, stderr=subprocess.STDOUT, timeout=120)
            if bot.returncode:
                raise RuntimeError(f'Bot assertions failed; see {directory / "bot.log"}')
            details = json.loads((directory / 'bot.json').read_text())
            if details['status'] != 'PASS' or len(details['cases']) != 3:
                raise RuntimeError('Bot did not validate all three toast styles')
            result['cases'] = details['cases']
            stop_server(ACTIVE)
            errors = [line for line in log.read_text(errors='replace').splitlines()
                      if re.search(r'\bERROR\b|\bSEVERE\b|Failed to send toast|Error occurred while', line)]
            if errors:
                raise RuntimeError('Server logged errors: ' + '\n'.join(errors[-10:]))
            result['status'] = 'PASS'
    except (Exception, KeyboardInterrupt) as error:
        result['error'] = str(error) or 'Interrupted'
        if isinstance(error, KeyboardInterrupt):
            result['status'] = 'INTERRUPTED'
    finally:
        stop_server(ACTIVE)
        ACTIVE = None
        result['seconds'] = round(time.monotonic() - start, 2)
    return result


def report(directory, results, plugin_sha):
    summary = {'pluginSha256': plugin_sha, 'results': results,
               'note': 'Protocol assertions only; visual rendering is not tested.'}
    (directory / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n')
    suite = ET.Element('testsuite', name='Minecraft server tests', tests=str(len(results)),
                       failures=str(sum(r['status'] in ('FAIL', 'INTERRUPTED') for r in results)),
                       skipped=str(sum(r['status'] == 'UNSUPPORTED' for r in results)))
    for result in results:
        case = ET.SubElement(suite, 'testcase', classname=result['server'], name=result['version'], time=str(result['seconds']))
        if result['status'] != 'PASS':
            ET.SubElement(case, 'skipped' if result['status'] == 'UNSUPPORTED' else 'failure',
                          message=result.get('error', result['status']))
    ET.ElementTree(suite).write(directory / 'junit.xml', encoding='utf-8', xml_declaration=True)
    rows = ['# Server test results', '', '| Server | Version | Result | Seconds |', '| --- | --- | --- | --- |']
    rows += [f'| {r["server"]} | {r["version"]} | {r["status"]} | {r["seconds"]} |' for r in results]
    rows += ['', 'These tests validate packets, not client rendering. Details: summary.json and each version’s logs.']
    (directory / 'summary.md').write_text('\n'.join(rows) + '\n')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plugin', type=Path, required=True)
    parser.add_argument('--versions', default='smoke', help='smoke, all, or comma-separated exact releases')
    parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
    parser.add_argument('--accept-eula', action='store_true', help='Accept https://aka.ms/MinecraftEULA for this run')
    parser.add_argument('--server-dir', type=Path, help='Use executable <version>.jar files instead of downloading Paper')
    parser.add_argument('--startup-timeout', type=int, default=240)
    args = parser.parse_args()
    args.plugin = args.plugin.resolve()
    if not args.plugin.is_file():
        parser.error('Build the test plugin first, or use the Test Servers Maven configuration.')
    config = json.loads(LOCAL.read_text()) if LOCAL.exists() else {}
    if not (args.accept_eula or config.get('acceptEula') is True):
        parser.error(f'Accept https://aka.ms/MinecraftEULA explicitly with --accept-eula or acceptEula: true in {LOCAL}')
    versions = SMOKE if args.versions == 'smoke' else matrix() if args.versions == 'all' else args.versions.split(',')
    if len(set(versions)) != len(versions) or any(v not in matrix() for v in versions):
        parser.error('Use unique adapter target versions from NMS/*/pom.xml.')
    CACHE.mkdir(exist_ok=True)
    # Prevent concurrent runs from racing npm or the download cache.
    lock = CACHE / 'server-tests.lock'
    try:
        lock.mkdir()
    except FileExistsError:
        parser.error(f'Another run is active. If it was forcibly killed, remove {lock}.')
    try:
        node = node_runtime()
        prepare_bot(node)
        run_dir = ROOT / 'target/server-tests' / time.strftime('%Y%m%d-%H%M%S')
        run_dir.mkdir(parents=True)
        results = []
        plugin_sha = hashlib.sha256(args.plugin.read_bytes()).hexdigest()
        for version in versions:
            print(f'\nTesting {version}...', flush=True)
            result = run_one(version, args, config, node, run_dir / version)
            results.append(result)
            report(run_dir, results, plugin_sha)
            print(f'{version}: {result["status"]} ({result["seconds"]}s) {result.get("error", "")}', flush=True)
            if result['status'] == 'INTERRUPTED':
                break
        print(f'\nReport: {run_dir / "summary.md"}', flush=True)
        # Unsupported versions must never turn an incomplete matrix green.
        return 0 if len(results) == len(versions) and all(r['status'] == 'PASS' for r in results) else 1
    finally:
        lock.rmdir()


if __name__ == '__main__':
    signal.signal(signal.SIGTERM, interrupt)
    try:
        sys.exit(main())
    except (Exception, KeyboardInterrupt) as error:
        print(f'Server tests failed: {error}', file=sys.stderr)
        sys.exit(1)
