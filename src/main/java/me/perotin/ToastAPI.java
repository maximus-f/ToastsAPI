package me.perotin;

import me.perotin.adapter.ToastAdapter;
import me.perotin.adapter.ToastAdapterFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the Toast API plugin.
 * This class provides the public API for sending toast notifications to players.
 */
public class ToastAPI extends JavaPlugin implements CommandExecutor {
    private static ToastAPI instance;
    private ToastAdapter adapter;
    private boolean enabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Create the adapter for the current server version
        ToastAdapterFactory factory = new ToastAdapterFactory(getLogger());
        adapter = factory.createAdapter();

        if (adapter == null) {
            getLogger().severe("Failed to initialize Toast API: Unsupported server version");
            getLogger().severe("Toast API currently only supports Minecraft 1.20.6 and 1.21.4 (v1_20_R3, v1_21_R1, v1_21_R3)");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register the toast command
        getCommand("toast").setExecutor(this);

        enabled = true;
        getLogger().info("Toast API enabled successfully");
    }

    @Override
    public void onDisable() {
        instance = null;
        adapter = null;
        enabled = false;
        getLogger().info("Toast API disabled");
    }

    /**
     * Gets the instance of the Toast API plugin.
     *
     * @return The instance of the Toast API plugin
     */
    public static ToastAPI getInstance() {
        return instance;
    }

    /**
     * Sends a toast notification to a player.
     *
     * @param player The player to send the toast to
     * @param icon The icon to display in the toast (material name)
     * @param message The message to display in the toast
     * @param style The style of the toast (GOAL, TASK, CHALLENGE)
     */
    public static void sendToast(Player player, String icon, String message, ToastStyle style) {
        if (instance == null || !instance.enabled || instance.adapter == null) {
            throw new IllegalStateException("Toast API is not enabled");
        }

        instance.adapter.sendToast(player, icon, message, style);
    }

    /**
     * Handles the execution of the toast command.
     * This command sends a test toast notification to the player.
     *
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The alias of the command that was used
     * @param args The arguments provided with the command
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Default values
        String icon = "paper";
        String message = "Test Toast|This is a test toast notification!";
        ToastStyle style = ToastStyle.GOAL;

        // Parse arguments if provided
        if (args.length >= 1) {
            icon = args[0];
            // Remove minecraft: prefix if present
            if (icon.startsWith("minecraft:")) {
                icon = icon.substring(10);
            }
        }

        if (args.length >= 2) {
            message = args[1];
            // If no pipe character is present, add a default description
            if (!message.contains("|")) {
                message = message + "|This is a test toast notification!";
            }
        }

        if (args.length >= 3) {
            try {
                style = ToastStyle.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("Invalid toast style. Using default style GOAL.");
            }
        }

        try {
            sendToast(player, icon, message, style);
            player.sendMessage("Toast notification sent!");
        } catch (Exception e) {
            player.sendMessage("Failed to send toast notification: " + e.getMessage());
        }

        return true;
    }
}
