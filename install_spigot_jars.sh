#!/usr/bin/env bash
# install_spigot_jars.sh
# Build & install remapped Spigot + Spigot-API jars into ~/.m2

set -euo pipefail

### -------- change this if you prefer another build dir ----------
BUILD_DIR="$HOME/spigot-builds"
BT_JAR="$BUILD_DIR/BuildTools.jar"
###################################################################

usage() { echo "Usage: $0 <mc-version> [more-versions ...]"; exit 1; }
[[ $# -lt 1 ]] && usage

# Ensure build dir exists
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

# Download latest BuildTools if not present
if [[ ! -f "$BT_JAR" ]]; then
  echo "Downloading BuildTools.jar ..."
  curl -fsSL \
    -o "$BT_JAR" \
    https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi

for VER in "$@"; do
  echo "=== Building Spigot $VER ==="
  java -jar "$BT_JAR" --rev "$VER" --remapped

  # Paths vary slightly by BuildTools versions; search recursively
  REMAPPED_JAR=$(find "$BUILD_DIR" -name "spigot-${VER}-R0.1-SNAPSHOT-remapped-mojang.jar" | head -n1)
  API_JAR=$(find "$BUILD_DIR" -name "spigot-api-${VER}-R0.1-SNAPSHOT.jar" | head -n1)

  if [[ -z "$REMAPPED_JAR" || -z "$API_JAR" ]]; then
    echo "❌  Could not locate built jars for $VER." >&2
    exit 2
  fi

  echo "Installing $REMAPPED_JAR into ~/.m2 ..."
  mvn -q install:install-file \
      -Dfile="$REMAPPED_JAR" \
      -DgroupId=org.spigotmc \
      -DartifactId=spigot \
      -Dversion="${VER}-R0.1-SNAPSHOT" \
      -Dclassifier=remapped-mojang \
      -Dpackaging=jar

  echo "Installing $API_JAR into ~/.m2 ..."
  mvn -q install:install-file \
      -Dfile="$API_JAR" \
      -DgroupId=org.spigotmc \
      -DartifactId=spigot-api \
      -Dversion="${VER}-R0.1-SNAPSHOT" \
      -Dpackaging=jar

  echo "✓  Installed Spigot $VER (remapped + API)."
done

echo "All requested versions processed."

