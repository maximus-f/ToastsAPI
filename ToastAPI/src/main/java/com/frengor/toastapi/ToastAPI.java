package com.frengor.toastapi;

import com.frengor.toastapi.nms.util.Versions;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Main class for the Toast API.
 * Provides methods to display custom toast notifications to players.
 */
public class ToastAPI {

    private static final String ADV_DESCRIPTION = "\n§7A notification.";

    /**
     * Checks whether this library includes an adapter for the current server release.
     * This does not initialize NMS classes or guarantee compatibility with server forks.
     *
     * @return Whether an adapter is available for the server release.
     */
    public static boolean isSupported() {
        return Versions.getNMSVersion().isPresent();
    }

    // Delay NMS initialization until after the caller's parameters and server version are checked.
    private static class ToastState {
        private static final MinecraftKeyWrapper ROOT_KEY;
        private static final MinecraftKeyWrapper NOTIFICATION_KEY;
        private static final AdvancementWrapper ROOT;

        static {
            try {
                ROOT_KEY = MinecraftKeyWrapper.craft("com.frengor", "root");
                NOTIFICATION_KEY = MinecraftKeyWrapper.craft("com.frengor", "notification");
                AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(
                        new ItemStack(Material.GRASS_BLOCK),
                        "§f§lNotifications§1§2§3§4§5§6§7§8§9§0",
                        "§7Notification page.\n§7Close and reopen advancements to hide.",
                        AdvancementFrameTypeWrapper.TASK,
                        0, 0,
                        "textures/block/stone.png"
                );
                ROOT = AdvancementWrapper.craftRootAdvancement(ROOT_KEY, display, 1);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize ToastAPI", e);
            }
        }
    }

    /**
     * Displays a custom toast notification to a player.
     *
     * @param player The player to show the toast to
     * @param icon The item icon to display in the toast
     * @param title The title text of the toast
     * @param frame The frame type of the toast (TASK, GOAL, or CHALLENGE)
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if the icon is air
     * @throws UnsupportedOperationException if the server release is unsupported
     * @throws IllegalStateException if the toast could not be constructed or sent
     */
    public static void displayCustomToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "AdvancementFrameType is null.");
        Preconditions.checkArgument(!icon.getType().isAir(), "ItemStack is air.");
        if (!isSupported()) {
            throw new UnsupportedOperationException("Unsupported Minecraft version: " + Bukkit.getBukkitVersion()
                    + ". Supported releases: " + Versions.SUPPORTED_RELEASES);
        }

        try {
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(
                    icon,
                    title,
                    ADV_DESCRIPTION,
                    frame.getNMSWrapper(),
                    1, 0,
                    true, false, false
            );
            AdvancementWrapper notification = AdvancementWrapper.craftBaseAdvancement(ToastState.NOTIFICATION_KEY, ToastState.ROOT, display, 1);

            // Send the advancement packet to display the toast
            Map<AdvancementWrapper, Integer> advancements = new HashMap<>();
            advancements.put(ToastState.ROOT, 1);
            advancements.put(notification, 1);
            PacketPlayOutAdvancementsWrapper.craftSendPacket(advancements).sendTo(player);

            // Immediately remove the advancement to prevent it from staying in the advancement screen
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(
                    new HashSet<>(Arrays.asList(ToastState.ROOT_KEY, ToastState.NOTIFICATION_KEY))).sendTo(player);
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new IllegalStateException("Failed to send toast", e);
        }
    }
}
