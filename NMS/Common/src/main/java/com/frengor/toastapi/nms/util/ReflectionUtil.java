package com.frengor.toastapi.nms.util;

import com.frengor.toastapi.nms.wrappers.AbstractWrapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Reflection helpers that also work when the library is relocated by a consuming plugin. */
public final class ReflectionUtil {

    public static boolean classExists(@NotNull String className) {
        return findClass(Objects.requireNonNull(className, "ClassName cannot be null.")) != null;
    }

    @Nullable
    public static Class<?> getNMSClass(@NotNull String name, @NotNull String mcPackage) {
        return findClass("net.minecraft." + mcPackage + '.' + name);
    }

    @Nullable
    public static Class<?> getCBClass(@NotNull String name) {
        return findClass(Bukkit.getServer().getClass().getPackage().getName() + '.' + name);
    }

    @Nullable
    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name, false, ReflectionUtil.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Finds the implementation for the current server without initializing it.
     *
     * @param clazz The common wrapper type.
     * @param <T> The wrapper type.
     * @return The version-specific implementation class.
     * @throws UnsupportedOperationException If the server release is unsupported.
     * @throws IllegalStateException If the required adapter is missing from the library.
     */
    @NotNull
    public static <T> Class<? extends T> getWrapperClass(@NotNull Class<T> clazz) {
        String version = Versions.getNMSVersion().orElseThrow(() -> new UnsupportedOperationException(
                "Unsupported Minecraft version: " + Bukkit.getBukkitVersion()
                        + ". Supported releases: " + Versions.SUPPORTED_RELEASES));
        // Derive the prefix from a class reference, which Maven Shade relocates reliably.
        String wrapperPackage = AbstractWrapper.class.getPackage().getName();
        String validPackage = wrapperPackage + '.';
        String name = clazz.getName();
        if (!name.startsWith(validPackage)) {
            throw new IllegalArgumentException("Invalid wrapper class " + name + '.');
        }
        String nmsPackage = wrapperPackage.substring(0, wrapperPackage.length() - ".wrappers".length());
        String implementation = nmsPackage + '.' + version + '.'
                + name.substring(validPackage.length()) + '_' + version;
        try {
            return Class.forName(implementation, false, clazz.getClassLoader()).asSubclass(clazz);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing ToastsAPI adapter " + implementation, e);
        }
    }

    private ReflectionUtil() {
    }
}
