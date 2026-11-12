package com.frengor.toastapi.nms;

import com.frengor.toastapi.nms.util.Versions;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/** Runs against each new adapter's real server classes, without starting a server or opening sockets. */
public class AdapterTest {
    private static ItemStack icon;

    @BeforeClass
    public static void initializeMinecraft() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        String release = System.getProperty("minecraft.version");
        String revision = Versions.getNMSVersion(release).orElseThrow();
        if (revision.startsWith("v26_")) {
            // 26.x binds default item components when registries load, after Bootstrap.
            Object lookup = Class.forName("net.minecraft.data.registries.VanillaRegistries")
                    .getMethod(revision.equals("v26_3_R1") ? "createWorldLookup" : "createLookup").invoke(null);
            Object initializers = BuiltInRegistries.class.getField("DATA_COMPONENT_INITIALIZERS").get(null);
            var pending = (Iterable<?>) initializers.getClass()
                    .getMethod("build", Class.forName("net.minecraft.core.HolderLookup$Provider"))
                    .invoke(initializers, lookup);
            var apply = Class.forName("net.minecraft.core.component.DataComponentInitializers$PendingComponents")
                    .getMethod("apply");
            for (Object components : pending) {
                apply.invoke(components);
            }
        }
        String craftPackage = "org.bukkit.craftbukkit." + (revision.startsWith("v26_") ? "" : revision + '.');
        Map<Class<?>, Object> registries = new HashMap<>();
        Bukkit.setServer((Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBukkitVersion" -> release;
                    case "getName", "getVersion" -> "Adapter test";
                    case "getLogger" -> Logger.getLogger("AdapterTest");
                    case "getItemFactory" -> Class.forName(craftPackage + "inventory.CraftItemFactory").getMethod("instance").invoke(null);
                    case "getUnsafe" -> Class.forName(craftPackage + "util.CraftMagicNumbers").getField("INSTANCE").get(null);
                    case "getRegistry" -> {
                        Class<?> type = (Class<?>) args[0];
                        Object registry = registries.get(type);
                        if (registry == null) {
                            if (type.getName().equals("org.bukkit.inventory.ItemType")) {
                                registry = Class.forName(craftPackage + "CraftRegistry")
                                        .getMethod("createRegistry", Class.class, RegistryAccess.class)
                                        .invoke(null, type, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
                            } else {
                                // Bukkit initializes all registry constants; this test only uses the item registry.
                                registry = Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[]{org.bukkit.Registry.class},
                                        (ignored, operation, values) -> { throw new UnsupportedOperationException(type + ": " + operation.getName()); });
                            }
                            registries.put(type, registry);
                        }
                        yield registry;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }));
        Class<?> craftItem = Class.forName(craftPackage + "inventory.CraftItemStack");
        java.lang.reflect.Method mirror;
        try {
            mirror = craftItem.getMethod("asBukkitMirror", net.minecraft.world.item.ItemStack.class);
        } catch (NoSuchMethodException ignored) {
            mirror = craftItem.getMethod("asCraftMirror", net.minecraft.world.item.ItemStack.class);
        }
        icon = (ItemStack) mirror.invoke(null, new net.minecraft.world.item.ItemStack(Items.DIAMOND));
    }

    @Test
    public void constructsCompletedToastAndRemovalPacketsForEveryFrame() throws Exception {
        MinecraftKeyWrapper rootKey = MinecraftKeyWrapper.craft("toastapi_test", "root");
        MinecraftKeyWrapper toastKey = MinecraftKeyWrapper.craft("toastapi_test", "notification");
        assertEquals("toastapi_test:notification", toastKey.toString());
        for (AdvancementFrameTypeWrapper frame : new AdvancementFrameTypeWrapper[]{
                AdvancementFrameTypeWrapper.TASK, AdvancementFrameTypeWrapper.GOAL, AdvancementFrameTypeWrapper.CHALLENGE}) {
            assertNotNull(frame);
            AdvancementDisplayWrapper rootDisplay = AdvancementDisplayWrapper.craft(icon, "Root", "", frame, 0, 0,
                    "textures/block/stone.png");
            AdvancementWrapper root = AdvancementWrapper.craftRootAdvancement(rootKey, rootDisplay, 1);
            assertEquals("minecraft:textures/block/stone.png", rootDisplay.getBackgroundTexture());
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, "Toast", "Description", frame,
                    1, 2, true, false, false);
            assertEquals("Toast", display.getTitle());
            assertEquals("Description", display.getDescription());
            assertTrue(display.doesShowToast());
            assertFalse(display.doesAnnounceToChat());
            assertEquals(1, display.getX(), 0);
            assertEquals(2, display.getY(), 0);
            assertEquals(icon.getType(), display.getIcon().getType());
            AdvancementWrapper toast = AdvancementWrapper.craftBaseAdvancement(toastKey, root, display, 1);
            assertEquals(1, toast.getMaxProgression());
            Object original = field(PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(root, 1, toast, 1)), "packet");
            assertTrue(((Map<?, ?>) field(original, "progress")).values().stream()
                    .allMatch(p -> ((AdvancementProgress) p).isDone()));
            Object send = roundTrip(original);
            boolean positioned = Versions.getNMSVersion().orElseThrow().equals("v26_3_R1");
            String resetField = positioned ? "shouldReset" : "reset";
            assertFalse((Boolean) field(send, resetField));
            Collection<?> added = (Collection<?>) field(send, "added");
            assertEquals(2, added.size());
            if (positioned) {
                // 26.3 moved coordinates from DisplayInfo into the packet's PositionedAdvancement.
                for (Object advancement : added) {
                    Object holder = field(advancement, "advancement");
                    boolean isToast = holder.getClass().getMethod("id").invoke(holder).equals(toastKey.toNMS());
                    assertEquals(isToast ? 1 : 0, (Float) field(advancement, "x"), 0);
                    assertEquals(isToast ? 2 : 0, (Float) field(advancement, "y"), 0);
                }
            }
            Map<?, ?> progress = (Map<?, ?>) field(send, "progress");
            assertEquals(2, progress.size());
            // Requirements are attached by the client after decoding; the wire carries completed criteria.
            assertTrue(progress.values().stream().allMatch(p -> ((AdvancementProgress) p).getCriterion("0").isDone()));
            Object remove = roundTrip(field(PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(rootKey, toastKey)), "packet"));
            assertFalse((Boolean) field(remove, resetField));
            assertEquals(Set.of(rootKey.toNMS(), toastKey.toNMS()), field(remove, "removed"));
            assertTrue(((Collection<?>) field(remove, "added")).isEmpty());
        }
    }

    private static Object roundTrip(Object packet) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            ClientboundUpdateAdvancementsPacket.STREAM_CODEC.encode(buffer, (ClientboundUpdateAdvancementsPacket) packet);
            assertTrue(buffer.readableBytes() > 0);
            Object decoded = ClientboundUpdateAdvancementsPacket.STREAM_CODEC.decode(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }

    private static Object field(Object owner, String name) throws Exception {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }
}
