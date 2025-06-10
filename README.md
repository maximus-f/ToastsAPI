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

Want to use ToastAPI in your plugin? Follow these steps:

### 1. Build and Install ToastAPI Locally

First, clone and build ToastAPI:
```bash
git clone https://github.com/maximus-f/ToastsAPI.git
cd ToastsAPI
mvn clean install
```

This installs ToastAPI to your local Maven repository with:
- **GroupId**: `com.frengor`
- **ArtifactId**: `toastapi` 
- **Version**: `1.0.0`

### 2. Add as Dependency

Add ToastAPI to your plugin's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>spigot-repo</id>
        <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <!-- Spigot API (provided by server) -->
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.21.5-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- ToastAPI (installed locally) -->
    <dependency>
        <groupId>com.frengor</groupId>
        <artifactId>toastapi</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 3. Shade ToastAPI into Your Plugin

Use Maven Shade Plugin to include ToastAPI in your plugin:

```xml
<build>
    <plugins>
        <!-- Shade ToastAPI into your plugin jar -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <minimizeJar>false</minimizeJar>
                
                <relocations>
                    <relocation>
                        <pattern>com.frengor.toastapi</pattern>
                        <shadedPattern>your.package.libs.com.frengor.toastapi</shadedPattern>
                    </relocation>
                </relocations>
                
                <artifactSet>
                    <includes>
                        <include>com.frengor:toastapi</include>
                    </includes>
                </artifactSet>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 4. Plugin.yml Configuration

**No dependency needed** - ToastAPI is shaded into your plugin:
```yaml
name: YourPlugin
version: 1.0.0
main: your.package.YourPlugin
api-version: 1.21
# No depend/soft-depend needed since ToastAPI is shaded
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

### Alternative: JitPack (if available)
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
*Note: With JitPack, add `depend: [ToastAPI]` to your plugin.yml*

📖 **Full Integration Guide:** See [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) for complete documentation and examples.  
📋 **Example Template:** See [example-pom.xml](example-pom.xml) for a complete working pom.xml template.

## Attribution
This project contains code derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, licensed under **GPL v3**. The NMS wrapper system and advancement handling code are based on this original work.

**Original UAA Authors:** fren_gor, EscanorTargaryen  
**Original Project:** https://github.com/frengor/UltimateAdvancementAPI  
**Original License:** GPL-3.0

## License
This project is licensed under the **GNU General Public License v3.0** (same as the original UAA project).

See `LICENSE` file for the full GPL v3 license terms.

