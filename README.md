# ToastAPI

A lightweight Minecraft plugin for displaying toast notifications across versions 1.19.4, 1.20.2, 1.20.4, 1.21, 1.21.4, 1.21.5

## Features
- Simple toast notifications
- Multi-version support (1.21, 1.20.1)
- Commands: `/toast <type> [message]`
- Join event notifications
- Permission system

## Installation
1. Download `ToastAPI-1.0.0.jar`
2. Place in your `plugins/` folder
3. Restart server

## Usage
```
/toast goal "Achievement unlocked!"
/toast task "Quest completed!"
/toast challenge "Boss defeated!"
```

## Permissions
- `toastapi.toast` - Use /toast command
- `toastapi.*` - All permissions

## For Developers

Want to use ToastAPI in your plugin? Add it as a dependency using JitPack:

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.maximus-f</groupId>
        <artifactId>ToastsAPI</artifactId>
        <version>v1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.maximus-f:ToastsAPI:v1.0.0'
}
```

### Plugin Dependencies (plugin.yml)
```yaml
depend: [ToastAPI]
# or soft-depend: [ToastAPI] if optional
```

### Basic Usage Example
```java
import com.frengor.toastapi.ToastAPI;
import com.frengor.toastapi.AdvancementFrameType;
import org.bukkit.Material;

// Send a toast notification
ToastAPI.getInstance().sendToast(
    player,
    Material.DIAMOND,
    "Achievement Unlocked!",
    AdvancementFrameType.GOAL
);
```

📖 **Full Integration Guide:** See [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) for complete documentation and examples.

## Attribution
This project contains code derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, licensed under **GPL v3**. The NMS wrapper system and advancement handling code are based on this original work.

**Original UAA Authors:** fren_gor, EscanorTargaryen  
**Original Project:** https://github.com/frengor/UltimateAdvancementAPI  
**Original License:** GPL-3.0

## License
This project is licensed under the **GNU General Public License v3.0** (same as the original UAA project).

See `LICENSE` file for the full GPL v3 license terms.

