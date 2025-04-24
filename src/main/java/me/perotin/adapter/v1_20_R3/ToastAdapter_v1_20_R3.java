package me.perotin.adapter.v1_20_R3;

import me.perotin.ToastStyle;
import me.perotin.adapter.ToastAdapter;
import me.perotin.core.ToastImpl;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

/**
 * Implementation of the ToastAdapter interface for Minecraft 1.20.6 (v1_20_R3).
 * This class uses Bukkit's Unsafe API to create and register advancements for toast notifications.
 */
public class ToastAdapter_v1_20_R3 implements ToastAdapter {

    @Override
    public void sendToast(Player player, String icon, String message, ToastStyle style) {
        // Create a custom implementation of ToastImpl that overrides createAdvancement
        ToastImpl toast = new ToastImpl(icon, message, style) {
            @Override
            protected void createAdvancement() {
                try {
                    // Create the advancement JSON
                    String frameType = getFrameType(getStyle());
                    String json = createAdvancementJson(getKey().getKey(), getIcon(), getMessage(), frameType);

                    // Use Bukkit's Unsafe API to load the advancement
                    Bukkit.getUnsafe().loadAdvancement(getKey(), json);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Start the toast notification process
        toast.start(player);
    }

    /**
     * Creates the JSON for an advancement.
     *
     * @param id The ID of the advancement
     * @param icon The icon to display in the toast
     * @param message The message to display in the toast
     * @param frameType The frame type (goal, task, challenge)
     * @return The JSON for the advancement
     */
    private String createAdvancementJson(String id, String icon, String message, String frameType) {
        // Split the message by | to get title and description
        String[] parts = message.split("\\|", 2);
        String title = escapeJson(parts[0]);
        String description = parts.length > 1 ? escapeJson(parts[1]) : "";

        // Create the JSON with the id field and properly escaped strings
        return "{"
            + "\"id\":\"minecraft:" + id + "\","  // Add the id field
            + "\"criteria\":{"
            + "\"trigger\":{"
            + "\"trigger\":\"minecraft:impossible\""
            + "}"
            + "},"
            + "\"display\":{"
            + "\"icon\":{"
            + "\"item\":\"minecraft:" + icon.toLowerCase() + "\""  // Ensure lowercase for item IDs
            + "},"
            + "\"title\":\"" + title + "\","
            + "\"description\":\"" + description + "\","
            + "\"background\":\"minecraft:textures/gui/advancements/backgrounds/adventure.png\","
            + "\"frame\":\"" + frameType + "\","
            + "\"announce_to_chat\":false,"
            + "\"show_toast\":true,"
            + "\"hidden\":true"
            + "}"
            + "}";
    }

    /**
     * Gets the frame type for a toast style.
     *
     * @param style The toast style
     * @return The frame type (goal, task, challenge)
     */
    private String getFrameType(ToastStyle style) {
        switch (style) {
            case GOAL:
                return "goal";
            case CHALLENGE:
                return "challenge";
            case TASK:
            default:
                return "task";
        }
    }

    /**
     * Escapes special characters in a string for JSON.
     *
     * @param input The string to escape
     * @return The escaped string
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    // Unicode escape for control characters
                    if (c < 32) {
                        String hex = Integer.toHexString(c);
                        escaped.append("\\u");
                        for (int i = 0; i < 4 - hex.length(); i++) {
                            escaped.append('0');
                        }
                        escaped.append(hex);
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }
}
