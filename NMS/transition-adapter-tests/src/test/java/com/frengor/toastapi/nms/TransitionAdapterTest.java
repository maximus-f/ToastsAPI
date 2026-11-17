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

/** Packet round trips against each real 1.19.4–1.21.5 server jar. */
public class TransitionAdapterTest {
    private static Class<?> bufferType;
    private static ItemStack icon;

    @BeforeClass
    public static void initializeMinecraft() throws Exception {
        final String release = System.getProperty("minecraft.version");
        String revision = Versions.getNMSVersion(release).orElseThrow(() -> new AssertionError(release));
        Class.forName("net.minecraft.SharedConstants").getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        bufferType = Class.forName("net.minecraft.network.FriendlyByteBuf");
        final String craft = "org.bukkit.craftbukkit." + revision + '.';
        Bukkit.setServer((Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getBukkitVersion": return release;
                        case "getName": case "getVersion": return "Transition adapter test";
                        case "getLogger": return Logger.getLogger("TransitionAdapterTest");
                        case "getItemFactory": return Class.forName(craft + "inventory.CraftItemFactory")
                                .getMethod("instance").invoke(null);
                        case "getUnsafe": return Class.forName(craft + "util.CraftMagicNumbers").getField("INSTANCE").get(null);
                        case "getRegistry":
                            Class<?> type = (Class<?>) args[0];
                            if (type.getName().equals("org.bukkit.inventory.ItemType")) {
                                return Class.forName(craft + "CraftRegistry").getMethod("createRegistry", Class.class,
                                        Class.forName("net.minecraft.core.RegistryAccess")).invoke(null, type, registries());
                            }
                            return Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{org.bukkit.Registry.class},
                                    (ignored, operation, values) -> { throw new UnsupportedOperationException(type + ": " + operation.getName()); });
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                }));
        try {
            Class.forName(craft + "CraftRegistry").getMethod("setMinecraftRegistry",
                    Class.forName("net.minecraft.core.RegistryAccess")).invoke(null, registries());
        } catch (NoSuchMethodException ignored) {
            // Earlier servers do not need a registry provider for chat serialization.
        }
        Class<?> craftItem = Class.forName(craft + "inventory.CraftItemStack");
        java.lang.reflect.Method mirror;
        try {
            mirror = craftItem.getMethod("asBukkitMirror", net.minecraft.world.item.ItemStack.class);
        } catch (NoSuchMethodException ignored) {
            mirror = craftItem.getMethod("asCraftMirror", net.minecraft.world.item.ItemStack.class);
        }
        icon = (ItemStack) mirror.invoke(null, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));
    }

    @Test
    public void completedToastAndRemovalSurvivePacketEncodingForEveryFrame() throws Exception {
        MinecraftKeyWrapper rootKey = MinecraftKeyWrapper.craft("toastapi_test", "root");
        MinecraftKeyWrapper toastKey = MinecraftKeyWrapper.craft("toastapi_test", "notification");
        assertEquals("toastapi_test:notification", toastKey.toString());
        assertEquals("toastapi_test", toastKey.getNamespace());
        assertEquals("notification", toastKey.getKey());
        assertEquals(0, toastKey.compareTo(MinecraftKeyWrapper.craft("toastapi_test", "notification")));
        assertTrue(toastKey.compareTo(rootKey) < 0);
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
            assertEquals(false, field(decoded, "reset"));
            assertEquals(new HashSet<>(Arrays.asList(rootKey.toNMS(), toastKey.toNMS())),
                    addedIds(decoded));
            assertTrue(((Set<?>) field(decoded, "removed")).isEmpty());
            assertEquals(2, progress(decoded).size());
            for (Object progress : progress(decoded).values()) {
                assertTrue(criterionIsDone(progress, "0"));
            }
            Set<MinecraftKeyWrapper> keys = new HashSet<>(Arrays.asList(rootKey, toastKey));
            Object removal = roundTrip(packet(PacketPlayOutAdvancementsWrapper.craftRemovePacket(keys)));
            assertEquals(false, field(removal, "reset"));
            assertEquals(new HashSet<>(Arrays.asList(rootKey.toNMS(), toastKey.toNMS())), field(removal, "removed"));
            assertTrue(addedIds(removal).isEmpty());
            assertTrue(progress(removal).isEmpty());
        }
    }

    @Test
    public void richTextAndCompletedProgressArePreserved() throws Exception {
        AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon,
                new TextComponent("Title"), new TextComponent("Description"), AdvancementFrameTypeWrapper.GOAL,
                3, 4, true, false, false);
        assertEquals("Title", display.getTitle());
        assertEquals("Description", display.getDescription());
        MinecraftKeyWrapper key = MinecraftKeyWrapper.craft("toastapi_test", "rich_text");
        AdvancementWrapper advancement = AdvancementWrapper.craftRootAdvancement(key, display, 1);
        assertEquals(1, advancement.getMaxProgression());
        Map<AdvancementWrapper, Integer> advancements = new HashMap<>();
        advancements.put(advancement, 1);
        Object original = packet(PacketPlayOutAdvancementsWrapper.craftSendPacket(advancements));
        Object progress = progress(original).get(key.toNMS());
        assertEquals(true, progress.getClass().getMethod("isDone").invoke(progress));
        Object decodedProgress = progress(roundTrip(original)).get(key.toNMS());
        assertTrue(criterionIsDone(decodedProgress, "0"));
    }

    private static boolean criterionIsDone(Object progress, String name) throws Exception {
        Object criterion = progress.getClass().getMethod("getCriterion", String.class)
                .invoke(progress, name);
        assertNotNull(name, criterion);
        return (Boolean) criterion.getClass().getMethod("isDone").invoke(criterion);
    }

    private static Map<?, ?> progress(Object packet) throws Exception {
        return (Map<?, ?>) field(packet, "progress");
    }

    private static Object packet(Object wrapper) throws Exception {
        return field(wrapper, "packet");
    }

    private static Object field(Object instance, String name) throws Exception {
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static Object registries() throws Exception {
        Class<?> registry = Class.forName("net.minecraft.core.Registry");
        return Class.forName("net.minecraft.core.RegistryAccess").getMethod("fromRegistryOfRegistries", registry)
                .invoke(null, Class.forName("net.minecraft.core.registries.BuiltInRegistries").getField("REGISTRY").get(null));
    }

    private static Set<?> addedIds(Object packet) throws Exception {
        Object added = field(packet, "added");
        if (added instanceof Map) return ((Map<?, ?>) added).keySet();
        Set<Object> ids = new HashSet<>();
        for (Object holder : (Iterable<?>) added) ids.add(holder.getClass().getMethod("id").invoke(holder));
        return ids;
    }

    private static Object roundTrip(Object packet) throws Exception {
        Object codec;
        try {
            codec = packet.getClass().getField("STREAM_CODEC").get(null);
        } catch (NoSuchFieldException ignored) {
            codec = null;
        }
        ByteBuf buffer;
        if (codec == null) {
            buffer = (ByteBuf) bufferType.getConstructor(ByteBuf.class).newInstance(Unpooled.buffer());
        } else {
            buffer = (ByteBuf) Class.forName("net.minecraft.network.RegistryFriendlyByteBuf")
                    .getConstructor(ByteBuf.class, Class.forName("net.minecraft.core.RegistryAccess"))
                    .newInstance(Unpooled.buffer(), registries());
        }
        try {
            Object decoded;
            if (codec == null) {
                packet.getClass().getMethod("write", bufferType).invoke(packet, buffer);
                assertTrue(buffer.readableBytes() > 0);
                decoded = packet.getClass().getDeclaredConstructor(bufferType).newInstance(buffer);
            } else {
                Class<?> streamCodec = Class.forName("net.minecraft.network.codec.StreamCodec");
                streamCodec.getMethod("encode", Object.class, Object.class).invoke(codec, buffer, packet);
                assertTrue(buffer.readableBytes() > 0);
                decoded = streamCodec.getMethod("decode", Object.class).invoke(codec, buffer);
            }
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
