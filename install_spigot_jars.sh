#!/usr/bin/env bash
# BuildTools installs server jars, API, POMs and remapping tables into ~/.m2.
# Select the appropriate Java version on PATH before invoking this script.
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <mc-version> [more-versions ...]" >&2
  exit 1
fi

TOASTS_BUILD_DIR="${TOASTS_BUILD_DIR:-$HOME/spigot-builds}"
mkdir -p "$TOASTS_BUILD_DIR"
cd "$TOASTS_BUILD_DIR"

if [[ ! -f BuildTools.jar ]]; then
  curl -fsSL -o BuildTools.jar \
    https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi

for toast_version in "$@"; do
  echo "Building Spigot $toast_version with BuildTools..."
  java -jar BuildTools.jar --rev "$toast_version" --remapped
done
