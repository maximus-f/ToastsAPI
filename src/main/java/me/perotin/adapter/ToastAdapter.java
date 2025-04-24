package me.perotin.adapter;

import me.perotin.ToastStyle;
import org.bukkit.entity.Player;

/**
 * Interface for version-specific toast notification implementations.
 * This abstraction layer allows for different implementations across Minecraft versions.
 */
public interface ToastAdapter {
    
    /**
     * Sends a toast notification to a player.
     *
     * @param player The player to send the toast to
     * @param icon The icon to display in the toast (material name)
     * @param message The message to display in the toast
     * @param style The style of the toast (GOAL, TASK, CHALLENGE)
     */
    void sendToast(Player player, String icon, String message, ToastStyle style);
}