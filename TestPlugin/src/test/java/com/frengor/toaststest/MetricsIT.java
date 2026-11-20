package com.frengor.toaststest;

import org.bukkit.Server;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MetricsIT {
    @Rule public TemporaryFolder plugins = new TemporaryFolder();

    @Test
    public void independentlyShadedHostsShareOneReporterAndReleaseItOnDisable() throws Exception {
        // Exercise the real bStats constructor with the server owner's opt-out in place.
        Path config = plugins.newFolder("bStats").toPath().resolve("config.yml");
        Files.writeString(config, "enabled: false\nserverUuid: 00000000-0000-0000-0000-000000000001\n");
        Map<String, EventExecutor> listeners = new HashMap<>();
        PluginManager manager = (PluginManager) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PluginManager.class}, (proxy, method, args) -> {
                    if (method.getName().equals("registerEvent")) {
                        String name = ((Plugin) args[4]).getName();
                        assertNull("Reporter registered twice", listeners.put(name, (EventExecutor) args[3]));
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        Server server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPluginManager" -> manager;
                    case "isPrimaryThread" -> true;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        Plugin first = host("First", server);
        Plugin second = host("Second", server);
        // Load these exact binaries: a library copy and a copy relocated inside the test plugin.
        try (var library = loader("library.jar", "com.frengor.toastapi.");
             var plugin = loader("plugin.jar", "com.frengor.toaststest.internal.toastapi.")) {
            var initializeA = library.loadClass("com.frengor.toastapi.ToastAPI").getMethod("initialize", Plugin.class);
            var initializeB = plugin.loadClass("com.frengor.toaststest.internal.toastapi.ToastAPI").getMethod("initialize", Plugin.class);
            initializeA.invoke(null, first);
            initializeA.invoke(null, first);
            initializeB.invoke(null, second);
            assertEquals(java.util.Set.of("First"), listeners.keySet());
            listeners.get("First").execute(null, new PluginDisableEvent(second));
            initializeB.invoke(null, second);
            assertFalse(listeners.containsKey("Second"));
            listeners.remove("First").execute(null, new PluginDisableEvent(first));
            initializeB.invoke(null, second);
            assertEquals(java.util.Set.of("Second"), listeners.keySet());
            listeners.remove("Second").execute(null, new PluginDisableEvent(second));
        }
    }

    private Plugin host(String name, Server server) {
        return (Plugin) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getServer" -> server;
                    case "isEnabled" -> true;
                    case "getDataFolder" -> new File(plugins.getRoot(), name);
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private URLClassLoader loader(String jar, String prefix) throws Exception {
        return new URLClassLoader(new URL[]{Path.of(System.getProperty(jar)).toUri().toURL()}, getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith(prefix)) return super.loadClass(name, resolve);
                Class<?> type = findLoadedClass(name);
                if (type == null) type = findClass(name);
                if (resolve) resolveClass(type);
                return type;
            }
        };
    }
}
