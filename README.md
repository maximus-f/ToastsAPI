# Toast API

A Minecraft plugin that provides an API for sending toast notifications to players. This plugin works on Paper/Spigot 1.20.6 and 1.21.4, and is designed to be extended to support other versions in the future.

## Features

- Send toast notifications to players using the advancement toast system
- Support for different toast styles (GOAL, TASK, CHALLENGE)
- Version-abstraction layer for future compatibility with other Minecraft versions

## Building

To build the plugin, you need Maven and Java 17 or higher.

```bash
mvn clean package
```

This will create a JAR file in the `target` directory named `toast-api-1.0-SNAPSHOT.jar`.

## Installation

1. Build the plugin or download the latest release
2. Place the JAR file in the `plugins` directory of your Paper/Spigot 1.20.6 or 1.21.4 server
3. Restart the server

## Usage

To use the Toast API in your plugin, add it as a dependency in your plugin.yml:

```yaml
depend: [toast-api]
```

Then, you can use the API to send toast notifications to players:

```java
import me.perotin.ToastAPI;
import me.perotin.ToastStyle;
import org.bukkit.entity.Player;

// ...

// Send a toast notification to a player
ToastAPI.sendToast(player, "paper", "Hello|world!", ToastStyle.GOAL);
```

The message parameter can include a pipe character (`|`) to separate the title and description of the toast. If no pipe character is present, the entire message is used as the title.

## Testing with the Built-in Command

The Toast API includes a built-in command for testing toast notifications without having to create another plugin:

```
/toast [icon] [message] [style]
```

Parameters:
- `icon` (optional): The icon to display in the toast (material name, e.g., "paper", "diamond", "apple"). Defaults to "paper".
- `message` (optional): The message to display in the toast. You can use a pipe character (`|`) to separate the title and description. Defaults to "Test Toast|This is a test toast notification!".
- `style` (optional): The style of the toast (GOAL, TASK, CHALLENGE). Defaults to GOAL.

Examples:
- `/toast` - Sends a default test toast
- `/toast diamond` - Sends a test toast with a diamond icon
- `/toast diamond "Custom Title|Custom description"` - Sends a toast with a custom title and description
- `/toast diamond "Custom Title"` - Sends a toast with a custom title and default description
- `/toast diamond "Custom Title|Custom description" CHALLENGE` - Sends a toast with a custom title, description, and style

This command requires the `toast.use` permission.

## Implementation Details

### Using Bukkit's API Instead of NMS

The Toast API uses Bukkit's Unsafe API to create and register advancements for toast notifications, avoiding direct NMS (net.minecraft.server) access whenever possible. This approach has several advantages:

1. **Better Compatibility**: Using Bukkit's API reduces the risk of breaking changes between Minecraft versions
2. **Cleaner Code**: No need for complex reflection to access NMS classes and methods
3. **Easier Maintenance**: Less version-specific code to maintain
4. **Future-Proofing**: Bukkit's API is more stable than NMS, which can change significantly between versions

The key part of the implementation uses Bukkit's Unsafe API to create advancements:

```java
// Create the advancement JSON
String frameType = getFrameType(getStyle());
String json = createAdvancementJson(getKey().getKey(), getIcon(), getMessage(), frameType);

// Use Bukkit's Unsafe API to load the advancement
Bukkit.getUnsafe().loadAdvancement(getKey(), json);
```

This approach eliminates the need for version-specific NMS code, which would require different implementations for each Minecraft version due to changes in the internal server code.

### Adding Support for Other Versions

The Toast API is designed to be extended to support other Minecraft versions. To add support for a new version:

1. Create a new package in `me.perotin.adapter` for the version (e.g., `v1_15_R1` for Minecraft 1.15)
2. Create a new class that implements the `ToastAdapter` interface
3. Update the `ToastAdapterFactory` class to recognize the new version

The current implementation supports 1.20.6 (v1_20_R3) and 1.21.4 (v1_21_R1, v1_21_R3). The adapter factory uses reflection to dynamically load the appropriate adapter based on the server version:

```java
// In ToastAdapterFactory.java
public ToastAdapter createAdapter() {
    String version = getServerVersion();

    // Supporting 1.20.6 (v1_20_R3) and 1.21.4 (v1_21_R1, v1_21_R3)
    if (version.equals("v1_20_R3") || version.equals("v1_21_R1") || version.equals("v1_21_R3")) {
        // Map v1_21_R3 to v1_21_R1 as they're both for 1.21.4
        if (version.equals("v1_21_R3")) {
            logger.info("Mapping v1_21_R3 to v1_21_R1 adapter");
            version = "v1_21_R1";
        }
        try {
            // Use reflection to create the adapter to avoid direct class references
            String adapterClassName = "me.perotin.adapter." + version + ".ToastAdapter_" + version;
            Class<?> adapterClass = Class.forName(adapterClassName);
            return (ToastAdapter) adapterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.severe("Failed to create toast adapter for version " + version + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    logger.warning("Unsupported server version: " + version);
    logger.warning("Toast API currently only supports Minecraft 1.20.6 and 1.21.4 (v1_20_R3, v1_21_R1, v1_21_R3)");
    return null;
}
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.