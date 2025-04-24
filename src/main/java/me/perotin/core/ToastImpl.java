package me.perotin.core;

import me.perotin.ToastAPI;
import me.perotin.ToastStyle;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Core implementation of toast notifications using the advancement packet trick.
 * This class is responsible for creating, granting, and revoking advancements
 * to show toast notifications to players.
 */
public class ToastImpl {
    private final NamespacedKey key;
    private final String icon;
    private final String message;
    private final ToastStyle style;

    /**
     * Creates a new toast implementation.
     *
     * @param icon The icon to display in the toast (material name)
     * @param message The message to display in the toast
     * @param style The style of the toast (GOAL, TASK, CHALLENGE)
     */
    public ToastImpl(String icon, String message, ToastStyle style) {
        this.key = new NamespacedKey(ToastAPI.getInstance(), UUID.randomUUID().toString());
        this.icon = icon;
        this.message = message;
        this.style = style;
    }

    /**
     * Starts the toast notification process for a player.
     * This creates an advancement, grants it to the player, and then revokes it after a delay.
     *
     * @param player The player to show the toast to
     */
    public void start(Player player) {
        createAdvancement();
        grant(player);
        Bukkit.getScheduler().runTaskLater(
            ToastAPI.getInstance(),
            () -> revoke(player),
            10L
        );
    }

    /**
     * Creates a temporary advancement for the toast notification.
     * This method should be implemented by version-specific adapters.
     */
    protected void createAdvancement() {
        // This will be implemented by version-specific adapters
    }

    /**
     * Grants the advancement to the player, triggering the toast notification.
     *
     * @param player The player to grant the advancement to
     */
    protected void grant(Player player) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone()) {
                for (String criteria : progress.getRemainingCriteria()) {
                    progress.awardCriteria(criteria);
                }
            }
        }
    }

    /**
     * Revokes the advancement from the player.
     *
     * @param player The player to revoke the advancement from
     */
    protected void revoke(Player player) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                for (String criteria : progress.getAwardedCriteria()) {
                    progress.revokeCriteria(criteria);
                }
            }
            Bukkit.getUnsafe().removeAdvancement(key);
        }
    }

    /**
     * Gets the key for this toast's advancement.
     *
     * @return The key for this toast's advancement
     */
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Gets the icon for this toast.
     *
     * @return The icon for this toast
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gets the message for this toast.
     *
     * @return The message for this toast
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the style for this toast.
     *
     * @return The style for this toast
     */
    public ToastStyle getStyle() {
        return style;
    }
}