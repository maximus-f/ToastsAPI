package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.ToastUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal API for displaying toast notifications to players.
 */
public class ToastAPI {

    /**
     * Displays a custom toast notification to a player.
     *
     * @param player The player to show the toast to.
     * @param icon The item to display in the toast.
     * @param title The title of the toast.
     * @param frame The frame type of the toast.
     */
    public static void displayCustomToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        ToastUtils.displayToast(player, icon, title, frame);
    }
} 