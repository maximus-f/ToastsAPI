package me.perotin.adapter;

import org.bukkit.Bukkit;
import java.util.logging.Logger;

/**
 * Factory for creating version-specific toast adapters.
 * This class is responsible for selecting the appropriate adapter implementation
 * based on the server version.
 */
public class ToastAdapterFactory {
    private final Logger logger;

    /**
     * Creates a new ToastAdapterFactory.
     *
     * @param logger The logger to use for logging messages
     */
    public ToastAdapterFactory(Logger logger) {
        this.logger = logger;
    }

    /**
     * Creates a toast adapter for the current server version.
     *
     * @return A toast adapter for the current server version, or null if the server version is not supported
     */
    public ToastAdapter createAdapter() {
        String version = getServerVersion();

        // Log the detected version for debugging
        logger.info("Detected server version: " + version);

        // Supporting 1.20.6 (v1_20_R3) and 1.21.4 (v1_21_R1, v1_21_R3)
        if (version.equals("v1_20_R3") || version.equals("v1_21_R1") || version.equals("v1_21_R3")) {
            // Map v1_21_R3 to v1_21_R1 as they're both for 1.21.4
            if (version.equals("v1_21_R3")) {
                logger.info("Mapping v1_21_R3 to v1_21_R1 adapter");
                version = "v1_21_R1";
            }
            try {
                // Use reflection to create the adapter to avoid direct class references
                String adapterClassName = "me.perotin.adapter." + version + ".ToastAdapter_" + version;
                Class<?> adapterClass = Class.forName(adapterClassName);
                return (ToastAdapter) adapterClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.severe("Failed to create toast adapter for version " + version + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        // If the version string contains the major version numbers, try to map it
        if (version.contains("1.20")) {
            logger.info("Mapping version " + version + " to v1_20_R3");
            return createAdapterForVersion("v1_20_R3");
        } else if (version.contains("1.21")) {
            logger.info("Mapping version " + version + " to v1_21_R1");
            return createAdapterForVersion("v1_21_R1");
        }

        logger.warning("Unsupported server version: " + version);
        logger.warning("Toast API currently only supports Minecraft 1.20.6 and 1.21.4 (v1_20_R3, v1_21_R1, v1_21_R3)");
        return null;
    }

    private ToastAdapter createAdapterForVersion(String version) {
        try {
            String adapterClassName = "me.perotin.adapter." + version + ".ToastAdapter_" + version;
            Class<?> adapterClass = Class.forName(adapterClassName);
            return (ToastAdapter) adapterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.severe("Failed to create toast adapter for version " + version + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the server version string (e.g., "v1_20_R3").
     *
     * @return The server version string
     */
    String getServerVersion() {
        try {
            // First attempt: Try to extract from package name using regex
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("v\\d+_\\d+_R\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(packageName);
            if (matcher.find()) {
                return matcher.group();
            }

            // Second attempt: Use Bukkit version methods
            String bukkitVersion = Bukkit.getBukkitVersion();
            // Example: "1.21.4-R0.1-SNAPSHOT"

            // Extract the major and minor version (e.g., "1.21")
            String[] versionParts = bukkitVersion.split("-")[0].split("\\.");
            if (versionParts.length >= 2) {
                String majorVersion = versionParts[0];
                String minorVersion = versionParts[1];

                // Map to the corresponding NMS version
                if (majorVersion.equals("1")) {
                    if (minorVersion.equals("20")) {
                        return "v1_20_R3"; // For 1.20.x
                    } else if (minorVersion.equals("21")) {
                        return "v1_21_R1"; // For 1.21.x
                    }
                }
            }

            // Log the actual version for debugging
            logger.info("Detected Bukkit version: " + bukkitVersion);
            logger.info("Server class package: " + packageName);

            // If all else fails, return the raw package name for debugging
            return packageName;
        } catch (Exception e) {
            logger.severe("Error determining server version: " + e.getMessage());
            e.printStackTrace();
            return "unknown";
        }
    }
}
