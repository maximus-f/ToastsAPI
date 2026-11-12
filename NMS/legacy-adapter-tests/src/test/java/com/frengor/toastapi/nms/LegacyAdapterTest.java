package com.frengor.toastapi.nms;

import com.frengor.toastapi.nms.util.Versions;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/** Shared Java 8 test source, compiled against each 1.15–1.18 adapter's real server jar. */
public class LegacyAdapterTest {
    private static boolean versionedNms;
    private static String revision;
    private static Class<?> bufferType;

    @BeforeClass
    public static void initializeMinecraft() throws Exception {
        final String release = System.getProperty("minecraft.version");
        revision = Versions.getNMSVersion(release).orElseThrow(() -> new AssertionError(release));
        versionedNms = revision.startsWith("v1_15_") || revision.startsWith("v1_16_");
        if (versionedNms) {
            Class.forName("net.minecraft.server." + revision + ".DispenserRegistry").getMethod("init").invoke(null);
        } else {
            Class.forName("net.minecraft.SharedConstants").getMethod("tryDetectVersion").invoke(null);
            Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        }
        bufferType = Class.forName(versionedNms ? "net.minecraft.server." + revision + ".PacketDataSerializer"
                : "net.minecraft.network.FriendlyByteBuf");
        final String craft = "org.bukkit.craftbukkit." + revision + '.';
        Bukkit.setServer((Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getBukkitVersion": return release;
                        case "getName": case "getVersion": return "Legacy adapter test";
                        case "getLogger": return Logger.getLogger("LegacyAdapterTest");
                        case "getItemFactory": return Class.forName(craft + "inventory.CraftItemFactory")
                                .getMethod("instance").invoke(null);
                        case "getUnsafe": return Class.forName(craft + "util.CraftMagicNumbers").getField("INSTANCE").get(null);
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                }));
    }

    @Test
    public void completedToastAndRemovalSurvivePacketEncodingForEveryFrame() throws Exception {
        MinecraftKeyWrapper rootKey = MinecraftKeyWrapper.craft("toastapi_test", "root");
        MinecraftKeyWrapper toastKey = MinecraftKeyWrapper.craft("toastapi_test", "notification");
        assertEquals("toastapi_test:notification", toastKey.toString());
        ItemStack icon = new ItemStack(Material.DIAMOND);
        for (AdvancementFrameTypeWrapper frame : new AdvancementFrameTypeWrapper[]{
                AdvancementFrameTypeWrapper.TASK, AdvancementFrameTypeWrapper.GOAL, AdvancementFrameTypeWrapper.CHALLENGE}) {
            AdvancementDisplayWrapper rootDisplay = AdvancementDisplayWrapper.craft(icon, "Root", "", frame,
                    0, 0, "textures/block/stone.png");
            assertEquals("minecraft:textures/block/stone.png", rootDisplay.getBackgroundTexture());
            AdvancementWrapper root = AdvancementWrapper.craftRootAdvancement(rootKey, rootDisplay, 1);
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, "Toast", "Description", frame,
                    1, 2, true, false, false);
            assertEquals(Material.DIAMOND, display.getIcon().getType());
            assertEquals("Toast", display.getTitle());
            assertEquals("Description", display.getDescription());
            assertSame(frame, display.getAdvancementFrameType());
            assertEquals(1, display.getX(), 0);
            assertEquals(2, display.getY(), 0);
            assertTrue(display.doesShowToast());
            assertFalse(display.doesAnnounceToChat());
            assertFalse(display.isHidden());
            assertNull(display.getBackgroundTexture());
            AdvancementWrapper toast = AdvancementWrapper.craftBaseAdvancement(toastKey, root, display, 1);
            assertEquals(1, toast.getMaxProgression());
            Map<AdvancementWrapper, Integer> advancements = new HashMap<>();
            advancements.put(root, 1);
            advancements.put(toast, 1);
            Object original = packet(PacketPlayOutAdvancementsWrapper.craftSendPacket(advancements));
            for (Object progress : progress(original).values()) {
                assertEquals(true, progress.getClass().getMethod("isDone").invoke(progress));
            }
            Object decoded = roundTrip(original);
            assertEquals(false, field(decoded, "a", "reset"));
            assertEquals(new HashSet<>(Arrays.asList(rootKey.toNMS(), toastKey.toNMS())),
                    ((Map<?, ?>) field(decoded, "b", "added")).keySet());
            assertTrue(((Set<?>) field(decoded, "c", "removed")).isEmpty());
            assertEquals(2, progress(decoded).size());
            for (Object progress : progress(decoded).values()) {
                assertTrue(criterionIsDone(progress, "0"));
            }
            Set<MinecraftKeyWrapper> keys = new HashSet<>(Arrays.asList(rootKey, toastKey));
            Object removal = roundTrip(packet(PacketPlayOutAdvancementsWrapper.craftRemovePacket(keys)));
            assertEquals(false, field(removal, "a", "reset"));
            assertEquals(new HashSet<>(Arrays.asList(rootKey.toNMS(), toastKey.toNMS())), field(removal, "c", "removed"));
            assertTrue(((Map<?, ?>) field(removal, "b", "added")).isEmpty());
            assertTrue(progress(removal).isEmpty());
        }
    }

    @Test
    public void richTextAndPartialProgressArePreserved() throws Exception {
        AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(new ItemStack(Material.DIAMOND),
                new TextComponent("Title"), new TextComponent("Description"), AdvancementFrameTypeWrapper.GOAL,
                3, 4, true, false, false);
        assertEquals("Title", display.getTitle());
        assertEquals("Description", display.getDescription());
        MinecraftKeyWrapper key = MinecraftKeyWrapper.craft("toastapi_test", "partial");
        AdvancementWrapper advancement = AdvancementWrapper.craftRootAdvancement(key, display, 2);
        assertEquals(2, advancement.getMaxProgression());
        Map<AdvancementWrapper, Integer> advancements = new HashMap<>();
        advancements.put(advancement, 1);
        Object original = packet(PacketPlayOutAdvancementsWrapper.craftSendPacket(advancements));
        Object progress = progress(original).get(key.toNMS());
        assertEquals(false, progress.getClass().getMethod("isDone").invoke(progress));
        Object decodedProgress = progress(roundTrip(original)).get(key.toNMS());
        assertTrue(criterionIsDone(decodedProgress, "0"));
        assertFalse(criterionIsDone(decodedProgress, "1"));
    }

    private static boolean criterionIsDone(Object progress, String name) throws Exception {
        Object criterion = progress.getClass().getMethod(versionedNms ? "getCriterionProgress" : "getCriterion", String.class)
                .invoke(progress, name);
        assertNotNull(name, criterion);
        return (Boolean) criterion.getClass().getMethod(versionedNms ? "a" : "isDone").invoke(criterion);
    }

    private static Map<?, ?> progress(Object packet) throws Exception {
        return (Map<?, ?>) field(packet, "d", "progress");
    }

    private static Object packet(Object wrapper) throws Exception {
        return field(wrapper, "packet", "packet");
    }

    private static Object field(Object instance, String spigotName, String mojangName) throws Exception {
        Field field = instance.getClass().getDeclaredField(versionedNms ? spigotName : mojangName);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static Object roundTrip(Object packet) throws Exception {
        ByteBuf buffer = (ByteBuf) bufferType.getConstructor(ByteBuf.class).newInstance(Unpooled.buffer());
        try {
            packet.getClass().getMethod(versionedNms ? "b" : "write", bufferType).invoke(packet, buffer);
            assertTrue(buffer.readableBytes() > 0);
            Object decoded;
            if (versionedNms) {
                decoded = packet.getClass().getConstructor().newInstance();
                decoded.getClass().getMethod("a", bufferType).invoke(decoded, buffer);
            } else {
                decoded = packet.getClass().getConstructor(bufferType).newInstance(buffer);
            }
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
