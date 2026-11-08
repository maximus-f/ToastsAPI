package com.frengor.toaststest;

import com.frengor.toastapi.AdvancementFrameType;
import com.frengor.toastapi.ToastAPI;
import com.frengor.toastapi.nms.util.Versions;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Level;

/**
 * Main plugin class for ToastAPI.
 * Standalone consumer used to test toast functionality.
 */
public class ToastPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Versions.getNMSVersion().isEmpty()) {
            getLogger().severe("Unsupported Minecraft version: " + getServer().getBukkitVersion()
                    + ". Supported releases: " + Versions.SUPPORTED_RELEASES);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("ToastAPI Plugin enabled! Use /toast <type> [message] to test toast notifications.");
        getLogger().info("Supported types: task, goal, challenge");
    }

    @Override
    public void onDisable() {
        getLogger().info("ToastAPI Plugin disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("toastapi.toast")) {
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
            frameType = AdvancementFrameType.valueOf(args[0].toUpperCase(Locale.ROOT));
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
            player.sendMessage("§aSent " + frameType.name().toLowerCase(Locale.ROOT) + " toast: §f" + message);
        } catch (RuntimeException | LinkageError e) {
            player.sendMessage("§cFailed to send toast: " + e.getMessage());
            getLogger().log(Level.WARNING, "Failed to send toast to " + player.getName(), e);
        }

        return true;
    }


}
