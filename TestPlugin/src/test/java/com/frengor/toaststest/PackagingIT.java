package com.frengor.toaststest;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/** Exercises the final artifacts, including relocation, rather than target/classes. */
public class PackagingIT {
    private static final String ORIGINAL = "com/frengor/toastapi/";
    private static final String RELOCATED = "com/frengor/toaststest/internal/toastapi/";
    private static final List<String> REVISIONS = List.of("v1_19_R3", "v1_20_R1", "v1_20_R2", "v1_20_R3",
            "v1_20_R4", "v1_21_R1", "v1_21_R2", "v1_21_R3", "v1_21_R4");

    @Test
    public void libraryContainsOnlyTheLibraryAndAllAdapters() throws Exception {
        try (JarFile library = new JarFile(System.getProperty("library.jar"))) {
            assertNull(library.getEntry("plugin.yml"));
            assertNull(library.getEntry("paper-plugin.yml"));
            assertFalse(library.stream().anyMatch(e -> e.getName().endsWith("ToastPlugin.class")));
            assertNotNull(library.getEntry(ORIGINAL + "ToastAPI.class"));
            for (String revision : REVISIONS) {
                assertNotNull(revision, library.getEntry(ORIGINAL + "nms/" + revision
                        + "/packets/PacketPlayOutAdvancementsWrapper_" + revision + ".class"));
            }
            for (String resource : List.of("LICENSE", "LGPL", "NOTICE")) {
                assertNotNull(resource, library.getEntry(resource));
            }
            // Server APIs and build/test dependencies must never be bundled.
            assertFalse(library.stream().anyMatch(e -> e.getName().endsWith(".class")
                    && !e.getName().startsWith(ORIGINAL)));
            for (var entry : library.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                byte[] bytes = library.getInputStream(entry).readAllBytes();
                int major = ((bytes[6] & 0xff) << 8) | (bytes[7] & 0xff);
                boolean modernAdapter = entry.getName().contains("/v1_20_R4/") || entry.getName().contains("/v1_21_");
                assertEquals(entry.getName(), modernAdapter ? 65 : 61, major);
            }
        }
    }

    @Test
    public void testPluginHasItsOwnDescriptorAndRelocatedLibrary() throws Exception {
        try (JarFile plugin = new JarFile(System.getProperty("plugin.jar"))) {
            assertEquals(1, plugin.stream().filter(e -> e.getName().equals("plugin.yml")).count());
            var descriptor = YamlConfiguration.loadConfiguration(new InputStreamReader(
                    plugin.getInputStream(plugin.getJarEntry("plugin.yml")), StandardCharsets.UTF_8));
            assertEquals("ToastsAPITest", descriptor.getString("name"));
            assertEquals(System.getProperty("project.version"), descriptor.getString("version"));
            assertNotNull(plugin.getEntry(descriptor.getString("main").replace('.', '/') + ".class"));
            assertEquals("toastapi.toast", descriptor.getString("commands.toast.permission"));
            assertNotNull(plugin.getEntry(RELOCATED + "ToastAPI.class"));
            assertFalse(plugin.stream().anyMatch(e -> e.getName().startsWith(ORIGINAL)));
        }
    }

    @Test
    public void relocatedLibraryFindsEveryBundledAdapterWithoutInitializingNms() throws Exception {
        String[] currentVersion = {"1.19.4"};
        Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBukkitVersion" -> currentVersion[0] + "-R0.1-SNAPSHOT";
                    case "getName", "getVersion" -> "Packaging test";
                    case "getLogger" -> Logger.getLogger("PackagingIT");
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        Bukkit.setServer(server);
        String prefix = RELOCATED.replace('/', '.');
        try (var loader = new URLClassLoader(new java.net.URL[]{Path.of(System.getProperty("plugin.jar")).toUri().toURL()},
                getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                // Failsafe also places the packaged plugin on its classpath. Force library
                // classes to come from this jar while sharing the mocked Bukkit API.
                if (!name.startsWith(prefix)) {
                    return super.loadClass(name, resolve);
                }
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    loaded = findClass(name);
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        }) {
            Class<?> api = loader.loadClass(prefix + "ToastAPI");
            assertSame(loader, api.getClassLoader());
            Class<?> reflection = loader.loadClass(prefix + "nms.util.ReflectionUtil");
            Class<?> key = loader.loadClass(prefix + "nms.wrappers.MinecraftKeyWrapper");
            String[] releases = {"1.19.4", "1.20.1", "1.20.2", "1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.4", "1.21.5"};
            for (int i = 0; i < releases.length; i++) {
                currentVersion[0] = releases[i];
                assertEquals(true, api.getMethod("isSupported").invoke(null));
                Class<?> adapter = (Class<?>) reflection.getMethod("getWrapperClass", Class.class).invoke(null, key);
                assertEquals(prefix + "nms." + REVISIONS.get(i) + ".MinecraftKeyWrapper_" + REVISIONS.get(i), adapter.getName());
                assertSame(loader, adapter.getClassLoader());
            }
            currentVersion[0] = "1.21.6";
            assertEquals(false, api.getMethod("isSupported").invoke(null));
            try {
                reflection.getMethod("getWrapperClass", Class.class).invoke(null, key);
                fail("An unsupported version must fail explicitly");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertTrue(e.getCause() instanceof UnsupportedOperationException);
                assertTrue(e.getCause().getMessage().contains("1.21.6"));
            }
        }
    }
}
