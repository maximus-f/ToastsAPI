#!/usr/bin/env bash
# BuildTools installs server jars, API, POMs and remapping tables into ~/.m2.
# Select the appropriate Java version with JAVA_HOME (or PATH) before invoking this script.
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <mc-version> [more-versions ...]" >&2
  exit 1
fi

TOASTS_BUILD_DIR="${TOASTS_BUILD_DIR:-$HOME/spigot-builds}"
mkdir -p "$TOASTS_BUILD_DIR"
cd "$TOASTS_BUILD_DIR"

# Refresh BuildTools: older copies cannot build the unobfuscated 26.x server.
curl -fsSL --retry 2 -o BuildTools.jar.download \
  https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
mv BuildTools.jar.download BuildTools.jar

toast_java="java"
if [[ -n "${JAVA_HOME:-}" ]]; then
  toast_java="$JAVA_HOME/bin/java"
fi

for toast_version in "$@"; do
  echo "Building Spigot $toast_version with BuildTools..."
  toast_args=(--rev "$toast_version")
  # Minecraft 26.x ships unobfuscated; BuildTools no longer produces mapping jars.
  if [[ "$toast_version" == 1.* ]]; then
    toast_args+=(--remapped)
  fi
  "$toast_java" -jar BuildTools.jar "${toast_args[@]}"
done
