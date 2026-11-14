package com.frengor.toastapi.nms.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Maps only releases covered by the adapters included in the distribution. */
public final class Versions {

    public static final String SUPPORTED_RELEASES = "1.15–1.15.2, 1.16–1.16.5, 1.17–1.17.1, 1.18–1.18.2, 1.19–1.19.4, 1.20–1.20.6, 1.21–1.21.11, 26.1–26.1.2, 26.2, 26.3";

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
     * Resolves a Minecraft release, Spigot version such as {@code 1.20.4-R0.1-SNAPSHOT},
     * or Paper version such as {@code 26.3.build.10-stable}.
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
        // Strip known server build suffixes, preserving Minecraft's -pre/-rc/-snapshot suffixes.
        String release = bukkitVersion.replaceFirst(
                "(?:-R[0-9]+\\.[0-9]+-SNAPSHOT|\\.build\\.[0-9]+-[a-z]+|\\.local-SNAPSHOT)$", "");
        switch (release) {
            case "1.15":
            case "1.15.1":
            case "1.15.2":
                return Optional.of("v1_15_R1");
            case "1.16":
            case "1.16.1":
                return Optional.of("v1_16_R1");
            case "1.16.2":
            case "1.16.3":
                return Optional.of("v1_16_R2");
            case "1.16.4":
            case "1.16.5":
                return Optional.of("v1_16_R3");
            case "1.17":
            case "1.17.1":
                return Optional.of("v1_17_R1");
            case "1.18":
            case "1.18.1":
                return Optional.of("v1_18_R1");
            case "1.18.2":
                return Optional.of("v1_18_R2");
            case "1.19":
            case "1.19.1":
            case "1.19.2":
                return Optional.of("v1_19_R1");
            case "1.19.3":
                return Optional.of("v1_19_R2");
            case "1.19.4":
                return Optional.of("v1_19_R3");
            case "1.20":
            case "1.20.1":
                return Optional.of("v1_20_R1");
            case "1.20.2":
                return Optional.of("v1_20_R2");
            case "1.20.3":
            case "1.20.4":
                return Optional.of("v1_20_R3");
            case "1.20.5":
            case "1.20.6":
                return Optional.of("v1_20_R4");
            case "1.21":
            case "1.21.1":
                return Optional.of("v1_21_R1");
            case "1.21.2":
            case "1.21.3":
                return Optional.of("v1_21_R2");
            case "1.21.4":
                return Optional.of("v1_21_R3");
            case "1.21.5":
                return Optional.of("v1_21_R4");
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                return Optional.of("v1_21_R5");
            case "1.21.9":
            case "1.21.10":
                return Optional.of("v1_21_R6");
            case "1.21.11":
                return Optional.of("v1_21_R7");
            case "26.1":
            case "26.1.1":
            case "26.1.2":
                return Optional.of("v26_1_R2");
            case "26.2":
                return Optional.of("v26_2_R1");
            case "26.3":
                return Optional.of("v26_3_R1");
            default: return Optional.empty();
        }
    }

    private Versions() {
    }
}
