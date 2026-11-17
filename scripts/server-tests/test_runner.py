"""Checks for runner failure handling, without downloading or starting Minecraft."""
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET

spec = importlib.util.spec_from_file_location('server_runner', Path(__file__).with_name('run.py'))
runner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runner)


class RunnerTest(unittest.TestCase):
    def test_paper_prefers_stable_and_records_development_fallback(self):
        def build(number, channel):
            return {'id': number, 'channel': channel, 'downloads': {'server:default': {
                'name': 'paper.jar', 'url': 'https://example.invalid/paper.jar',
                'checksums': {'sha256': 'hash'}}}}

        def fake_download(url, destination, digest):
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.touch()

        with tempfile.TemporaryDirectory() as temp, patch.object(runner, 'CACHE', Path(temp)), \
                patch.object(runner, 'download', side_effect=fake_download):
            with patch.object(runner, 'fetch', return_value=json.dumps([build(1, 'STABLE'), build(2, 'ALPHA')]).encode()):
                _, number, channel = runner.paper_jar('stable-release')
                self.assertEqual((1, 'STABLE'), (number, channel))
            with patch.object(runner, 'fetch', return_value=json.dumps([build(2, 'ALPHA'), build(3, 'ALPHA')]).encode()):
                _, number, channel = runner.paper_jar('alpha-only-release')
                self.assertEqual((3, 'ALPHA'), (number, channel))
            with patch.object(runner, 'fetch') as fetch:
                runner.paper_jar('alpha-only-release')
                fetch.assert_not_called()  # Selected builds remain pinned on subsequent runs.

    def test_download_rejects_corrupt_data_without_replacing_cache(self):
        with tempfile.TemporaryDirectory() as temp, patch.object(runner, 'fetch', return_value=b'corrupt'):
            path = Path(temp) / 'server.jar'
            path.write_bytes(b'existing')
            with self.assertRaisesRegex(RuntimeError, 'Checksum mismatch'):
                runner.download('https://example.invalid/server.jar', path, '0' * 64)
            self.assertEqual(b'existing', path.read_bytes())

    def test_eula_is_required_before_any_download_or_server_start(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            plugin = root / 'plugin.jar'
            plugin.touch()
            with patch.object(runner, 'LOCAL', root / 'missing.json'), \
                    patch.object(sys, 'argv', ['run.py', '--plugin', str(plugin)]), \
                    patch.object(runner, 'node_runtime') as node:
                with self.assertRaises(SystemExit) as error:
                    runner.main()
                self.assertEqual(2, error.exception.code)
                node.assert_not_called()

    def test_report_preserves_failures_and_unsupported_versions(self):
        results = [dict(server='paper', version=v, status=s, seconds=1, error='detail')
                   for v, s in [('1.19.4', 'PASS'), ('1.20.6', 'FAIL'), ('26.3', 'UNSUPPORTED')]]
        with tempfile.TemporaryDirectory() as temp:
            directory = Path(temp)
            runner.report(directory, results, 'hash')
            suite = ET.parse(directory / 'junit.xml').getroot()
            self.assertEqual('1', suite.get('failures'))
            self.assertEqual('1', suite.get('skipped'))
            self.assertEqual(1, len(suite.findall('testcase/failure')))
            self.assertEqual(1, len(suite.findall('testcase/skipped')))
            self.assertEqual(results, json.loads((directory / 'summary.json').read_text())['results'])

    def test_shutdown_sends_stop_and_reaps_process(self):
        process = subprocess.Popen([sys.executable, '-c',
                                    'import sys; sys.exit(0 if input() == "stop" else 1)'],
                                   stdin=subprocess.PIPE, text=True)
        runner.stop_server(process)
        self.assertEqual(0, process.returncode)

    def test_unsupported_matrix_returns_failure_and_releases_lock(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            plugin = root / 'plugin.jar'
            plugin.write_bytes(b'jar')
            result = dict(server='paper', version='26.3', status='UNSUPPORTED', seconds=0, error='No protocol')
            with patch.object(runner, 'ROOT', root), patch.object(runner, 'CACHE', root / 'cache'), \
                    patch.object(runner, 'LOCAL', root / 'missing.json'), \
                    patch.object(runner, 'matrix', return_value=['26.3']), \
                    patch.object(runner, 'node_runtime', return_value=Path('node')), \
                    patch.object(runner, 'prepare_bot'), patch.object(runner, 'run_one', return_value=result), \
                    patch.object(sys, 'argv', ['run.py', '--plugin', str(plugin), '--versions', 'all', '--accept-eula']):
                self.assertEqual(1, runner.main())
                self.assertFalse((root / 'cache/server-tests.lock').exists())


if __name__ == '__main__':
    unittest.main()
