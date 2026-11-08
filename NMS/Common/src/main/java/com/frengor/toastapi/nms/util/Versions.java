package com.frengor.toastapi.nms.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Maps only releases covered by the adapters included in the distribution. */
public final class Versions {

    public static final String SUPPORTED_RELEASES = "1.19.4, 1.20–1.20.6, 1.21–1.21.5";

    /**
     * Gets the adapter for the current server.
     *
     * @return The adapter revision, or empty for an unsupported release.
     */
    @NotNull
    public static Optional<String> getNMSVersion() {
        return getNMSVersion(Bukkit.getBukkitVersion());
    }

    /**
     * Resolves a Minecraft release or a Bukkit version such as {@code 1.20.4-R0.1-SNAPSHOT}.
     * Unknown versions, including prereleases and future patches, are rejected.
     *
     * @param bukkitVersion The release or Bukkit version to resolve.
     * @return The adapter revision, or empty for an unsupported release.
     */
    @NotNull
    public static Optional<String> getNMSVersion(@Nullable String bukkitVersion) {
        if (bukkitVersion == null) {
            return Optional.empty();
        }
        // Strip Bukkit's build suffix, but not Minecraft's -pre/-rc suffixes.
        String release = bukkitVersion.replaceFirst("-R[0-9]+\\.[0-9]+-SNAPSHOT$", "");
        return Optional.ofNullable(switch (release) {
            case "1.19.4" -> "v1_19_R3";
            case "1.20", "1.20.1" -> "v1_20_R1";
            case "1.20.2" -> "v1_20_R2";
            case "1.20.3", "1.20.4" -> "v1_20_R3";
            case "1.20.5", "1.20.6" -> "v1_20_R4";
            case "1.21", "1.21.1" -> "v1_21_R1";
            case "1.21.2", "1.21.3" -> "v1_21_R2";
            case "1.21.4" -> "v1_21_R3";
            case "1.21.5" -> "v1_21_R4";
            default -> null;
        });
    }

    private Versions() {
    }
}
