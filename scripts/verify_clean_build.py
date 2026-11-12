#!/usr/bin/env python3
"""Build with a fresh Maven repository, seeding only Spigot BuildTools outputs."""

import argparse
import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--buildtools-repository", type=Path, default=Path.home() / ".m2/repository")
    parser.add_argument("--repository", type=Path, help="An empty destination; defaults to a new temporary directory")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    source = args.buildtools_repository.expanduser().resolve()
    destination = args.repository.expanduser().resolve() if args.repository else Path(
        tempfile.mkdtemp(prefix="toastapi-maven-"))
    if destination.exists() and any(destination.iterdir()):
        parser.error(f"Destination must be empty: {destination}")

    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    modules = ET.parse(root / "NMS/pom.xml").findall("m:modules/m:module", ns)
    required = [Path("org/spigotmc/spigot-parent")]
    for module in modules:
        version = ET.parse(root / "NMS" / module.text / "pom.xml").findtext("m:properties/m:mc-version", namespaces=ns)
        if version:
            required.append(Path("org/spigotmc/spigot") / version)
            # 1.15/1.16 use the plain, versioned server jar and have no separate
            # minecraft-server mapping artifacts in BuildTools' Maven output.
            if not version.startswith(("1.15.", "1.16.")):
                required.append(Path("org/spigotmc/minecraft-server") / version)
    for path in required:
        if not (source / path).is_dir():
            parser.error(f"Missing BuildTools output: {source / path}. See README.md for the required revisions.")
    destination.mkdir(parents=True, exist_ok=True)
    for path in required:
        shutil.copytree(source / path, destination / path,
                        ignore=shutil.ignore_patterns("*.lastUpdated", "resolver-status.properties"))
    # No ToastsAPI/UAA artifacts, Maven plugins, or downloaded dependency caches are copied.
    assert not (destination / "com/frengor").exists()
    (destination / "toastapi-seed-manifest.json").write_text(json.dumps([str(p) for p in required], indent=2) + "\n")
    print(f"Isolated Maven repository: {destination}", flush=True)
    print("Seeded only Spigot server/mapping/parent artifacts; all other dependencies will be downloaded.", flush=True)
    result = subprocess.run(["mvn", "-B", "-ntp", f"-Dmaven.repo.local={destination}", "clean", "verify"], cwd=root)
    raise SystemExit(result.returncode)


if __name__ == "__main__":
    main()
