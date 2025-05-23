#!/usr/bin/env bash
# deps_of_setB.sh
# Usage: ./deps_of_setB.sh path/to/UltimateAdvancementAPI-Plugin-2.5.1.jar
# ------------------------------------------------------------------------

JAR="$1"
PKG_PREFIX="com.fren_gor"

# ------- Seed set B -----------------------------------------------------
read -r -d '' -a CLASSES <<'EOF'
com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper
com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper
com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper
com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper
com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper
EOF
# ------------------------------------------------------------------------

if [[ -z "$JAR" || ! -f "$JAR" ]]; then
  echo "Usage: $0 <path-to-plugin.jar>" >&2
  exit 1
fi

# Run jdeps:  -R recurse,  -verbose:class for class-level edges,
#             -filter keeps only in-package targets
jdeps -R -verbose:class -filter "$PKG_PREFIX" "$JAR" "${CLASSES[@]}" \
  | awk '{print $3}' \
  | sort -u

