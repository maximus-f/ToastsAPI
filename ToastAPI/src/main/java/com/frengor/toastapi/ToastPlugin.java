package com.frengor.toastapi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for ToastAPI.
 * Provides commands and events to test toast functionality.
 */
public class ToastPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        
        getLogger().info("ToastAPI Plugin enabled! Use /toast <type> [message] to test toast notifications.");
        getLogger().info("Supported types: task, goal, challenge");
    }

    @Override
    public void onDisable() {
        getLogger().info("ToastAPI Plugin disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("toastapi.usecmd")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (!command.getName().equalsIgnoreCase("toast")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eUsage: /toast <task|goal|challenge> [message]");
            player.sendMessage("§eExample: /toast goal Welcome to the server!");
            return true;
        }

        // Parse frame type
        AdvancementFrameType frameType;
        try {
            frameType = AdvancementFrameType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid toast type! Use: task, goal, or challenge");
            return true;
        }

        // Parse message
        String message = "Test Toast Notification";
        if (args.length > 1) {
            message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        }

        // Choose icon based on frame type
        ItemStack icon = switch (frameType) {
            case TASK -> new ItemStack(Material.GRASS_BLOCK);
            case GOAL -> new ItemStack(Material.DIAMOND);
            case CHALLENGE -> new ItemStack(Material.DRAGON_HEAD);
        };

        try {
            ToastAPI.displayCustomToast(player, icon, "§a" + message, frameType);
            player.sendMessage("§aSent " + frameType.name().toLowerCase() + " toast: §f" + message);
        } catch (Exception e) {
            player.sendMessage("§cFailed to send toast: " + e.getMessage());
            getLogger().warning("Failed to send toast to " + player.getName() + ": " + e.getMessage());
        }

        return true;
    }

    
} 