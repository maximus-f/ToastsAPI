package com.frengor.toastapi.nms.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Utility class for version detection.
 */
public class Versions {

    private static final String MINECRAFT_VERSION = Bukkit.getBukkitVersion().split("-")[0];
    private static final int VERSION = Integer.parseInt(MINECRAFT_VERSION.split("\\.")[1]);
    private static final int MINOR_VERSION;

    static {
        var splitted = MINECRAFT_VERSION.split("\\.");
        if (splitted.length > 2) {
            MINOR_VERSION = Integer.parseInt(splitted[2]);
        } else {
            MINOR_VERSION = 0;
        }
    }

    /**
     * Gets the NMS version string for the current server.
     *
     * @return The NMS version string, or empty if unsupported
     */
    @NotNull
    public static Optional<String> getNMSVersion() {
        return switch (VERSION) {
            case 15 -> Optional.of("v1_15_R1");
            case 16 -> {
                if (MINOR_VERSION == 1) {
                    yield Optional.of("v1_16_R1");
                } else if (MINOR_VERSION >= 4) {
                    yield Optional.of("v1_16_R3");
                } else {
                    yield Optional.of("v1_16_R2");
                }
            }
            case 17 -> Optional.of("v1_17_R1");
            case 18 -> {
                if (MINOR_VERSION >= 2) {
                    yield Optional.of("v1_18_R2");
                } else {
                    yield Optional.of("v1_18_R1");
                }
            }
            case 19 -> {
                if (MINOR_VERSION >= 4) {
                    yield Optional.of("v1_19_R3");
                } else if (MINOR_VERSION == 3) {
                    yield Optional.of("v1_19_R2");
                } else {
                    yield Optional.of("v1_19_R1");
                }
            }
            case 20 -> {
                if (MINOR_VERSION >= 6) {
                    yield Optional.of("v1_20_R4");
                } else if (MINOR_VERSION >= 4) {
                    yield Optional.of("v1_20_R3");
                } else if (MINOR_VERSION >= 2) {
                    yield Optional.of("v1_20_R2");
                } else {
                    yield Optional.of("v1_20_R1");
                }
            }
            case 21 -> {
                if (MINOR_VERSION >= 5) {
                    yield Optional.of("v1_21_R4");
                } else if (MINOR_VERSION >= 4) {
                    yield Optional.of("v1_21_R3");
                } else if (MINOR_VERSION >= 2) {
                    yield Optional.of("v1_21_R2");
                } else {
                    yield Optional.of("v1_21_R1");
                }
            }
            default -> Optional.empty();
        };
    }

    private Versions() {
        throw new UnsupportedOperationException("Utility class.");
    }
} 