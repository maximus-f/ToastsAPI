# 🔌 ToastAPI Integration Guide

A complete guide for developers who want to integrate ToastAPI into their Minecraft plugins.

> **💡 Quick Start**: Build ToastAPI locally (`mvn clean install`), then shade it into your plugin. No separate server-side installation needed!

## 📋 Table of Contents
- [Setup](#setup)
- [Basic Usage](#basic-usage)
- [Advanced Usage](#advanced-usage)
- [Distribution Options](#distribution-options)
- [Legal Considerations](#legal-considerations)

## 🛠️ Setup

### 1. **Build and Install ToastAPI Locally (RECOMMENDED)**

First, clone and build ToastAPI from source:
```bash
git clone https://github.com/maximus-f/ToastsAPI.git
cd ToastsAPI
mvn clean install
```

This installs ToastAPI to your local Maven repository with:
- **GroupId**: `com.frengor`
- **ArtifactId**: `toastapi` 
- **Version**: `1.0.0`

> 📋 **Quick Template**: See [example-pom.xml](example-pom.xml) for a complete working pom.xml you can copy and modify.

### 2. **Add ToastAPI Dependency (pom.xml)**
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

### 3. **Shade ToastAPI into Your Plugin**

Use Maven Shade Plugin to include ToastAPI directly in your plugin JAR:

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
                
                <!-- Only shade ToastAPI, not Spigot -->
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

### 4. **Plugin Configuration (plugin.yml)**

**No dependency needed** since ToastAPI is shaded into your plugin:
```yaml
name: YourPlugin
version: 1.0.0
main: your.package.YourPlugin
api-version: 1.21
# No depend/soft-depend needed - ToastAPI is included in your JAR
```

### Alternative: Manual Installation
If you have the ToastAPI JAR file, install it manually:
```bash
mvn install:install-file \
  -Dfile=ToastAPI-1.0.0.jar \
  -DgroupId=com.frengor \
  -DartifactId=toastapi \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

### Alternative: JitPack (if available)
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.maximus-f</groupId>
    <artifactId>ToastsAPI</artifactId>
    <version>v1.0.0</version>
    <scope>provided</scope>
</dependency>
```
*Note: With JitPack, add `depend: [ToastAPI]` to your plugin.yml*

## 📚 Basic Usage

### **Simple Toast Notification**
```java
import com.frengor.toastapi.ToastAPI;
import com.frengor.toastapi.AdvancementFrameType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // ToastAPI is shaded into your plugin - no dependency check needed!
        getLogger().info("Plugin enabled with ToastAPI support");
    }
    
    public void sendToast(Player player, String message) {
        try {
            ToastAPI.getInstance().sendToast(
                player,
                Material.DIAMOND,           // Icon
                "Achievement Unlocked!",    // Title
                AdvancementFrameType.GOAL  // Frame type
            );
        } catch (Exception e) {
            getLogger().warning("Failed to send toast: " + e.getMessage());
        }
    }
}
```

### **Event-Based Toasts**
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    // Welcome toast
    ToastAPI.getInstance().sendToast(
        player,
        Material.EMERALD,
        "Welcome to the server!",
        AdvancementFrameType.TASK
    );
}

@EventHandler
public void onPlayerLevelUp(PlayerLevelChangeEvent event) {
    if (event.getNewLevel() > event.getOldLevel()) {
        ToastAPI.getInstance().sendToast(
            event.getPlayer(),
            Material.EXPERIENCE_BOTTLE,
            "Level Up! (" + event.getNewLevel() + ")",
            AdvancementFrameType.CHALLENGE
        );
    }
}
```

## 🚀 Advanced Usage

### **Custom Toast Types**
```java
public enum CustomToastType {
    REWARD(Material.GOLD_INGOT, AdvancementFrameType.GOAL),
    WARNING(Material.TNT, AdvancementFrameType.CHALLENGE),
    INFO(Material.BOOK, AdvancementFrameType.TASK);
    
    private final Material icon;
    private final AdvancementFrameType frameType;
    
    CustomToastType(Material icon, AdvancementFrameType frameType) {
        this.icon = icon;
        this.frameType = frameType;
    }
    
    public void sendToast(Player player, String message) {
        ToastAPI.getInstance().sendToast(player, icon, message, frameType);
    }
}

// Usage:
CustomToastType.REWARD.sendToast(player, "You earned 100 coins!");
CustomToastType.WARNING.sendToast(player, "PvP zone entered!");
```

### **Bulk Toast Notifications**
```java
public void broadcastToast(String message, Material icon, AdvancementFrameType type) {
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.hasPermission("your.plugin.receive.toasts")) {
            ToastAPI.getInstance().sendToast(player, icon, message, type);
        }
    }
}
```

### **Delayed/Scheduled Toasts**
```java
public void scheduleToast(Player player, String message, long delayTicks) {
    Bukkit.getScheduler().runTaskLater(this, () -> {
        if (player.isOnline()) {
            ToastAPI.getInstance().sendToast(
                player,
                Material.CLOCK,
                message,
                AdvancementFrameType.TASK
            );
        }
    }, delayTicks);
}
```

## 📦 Distribution Options

### **Option 1: Shaded Plugin (RECOMMENDED)**
Include ToastAPI directly in your plugin JAR (as shown in setup above):
- ✅ **No server-side dependency management**
- ✅ **Users just drop your plugin in plugins folder**
- ✅ **No version conflicts**
- ✅ **Easiest for end users**

Example from a working project:
```xml
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
```

### **Option 2: Separate Plugin Dependency**
Users install ToastAPI as a separate plugin:
- ⚠️ **Users must install ToastAPI.jar separately**
- ⚠️ **Version compatibility issues possible**
- ✅ **Smaller plugin file size**

Use `depend: [ToastAPI]` in your plugin.yml

### **Option 3: JitPack (if available)**
If ToastAPI is hosted on GitHub with JitPack support:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### **Option 4: Maven Repository**
Host on Maven Central or your own Nexus repository for enterprise use.

## ⚖️ Legal Considerations

### **🚨 GPL License Requirements**

ToastAPI is licensed under **GPL-3.0** (inherited from UltimateAdvancementAPI).

#### **What This Means:**
- ✅ You can use ToastAPI in your plugins
- ✅ You can distribute your plugin commercially
- ⚠️ Your plugin **may need to be GPL-licensed** too
- ✅ You must provide source code if requested
- ✅ You must include GPL license notice

#### **GPL Compatibility:**
```java
/*
 * This plugin uses ToastAPI, which is licensed under GPL-3.0.
 * As a result, this plugin is also licensed under GPL-3.0.
 * 
 * ToastAPI: https://github.com/YourGitHub/ToastAPI
 * Original UAA: https://github.com/frengor/UltimateAdvancementAPI
 */
```

#### **Recommended License Header:**
```java
/*
 * Copyright (C) 2025 YourName
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
```

## 🎯 Best Practices

### **1. Error Handling**
```java
public boolean sendSafeToast(Player player, String message) {
    try {
        if (!player.isOnline()) return false;
        
        ToastAPI.getInstance().sendToast(
            player, Material.EMERALD, message, AdvancementFrameType.TASK
        );
        return true;
    } catch (Exception e) {
        getLogger().warning("Failed to send toast to " + player.getName() + ": " + e.getMessage());
        return false;
    }
}
```

### **2. Permission Checks**
```java
public void sendToastWithPermission(Player player, String message) {
    if (player.hasPermission("your.plugin.receive.toasts")) {
        ToastAPI.getInstance().sendToast(player, Material.BELL, message, AdvancementFrameType.TASK);
    }
}
```

### **3. Configuration Options**
```yaml
# config.yml
toasts:
  enabled: true
  show-join-message: true
  show-level-up: true
  custom-messages:
    welcome: "Welcome to our server!"
    level-up: "Congratulations! Level {level}"
```

## 🔗 Useful Links

- **ToastAPI GitHub**: https://github.com/maximus-f/ToastsAPI
- **Original UAA**: https://github.com/frengor/UltimateAdvancementAPI  
- **Maven Shade Plugin**: https://maven.apache.org/plugins/maven-shade-plugin/
- **GPL-3.0 License**: https://www.gnu.org/licenses/gpl-3.0.html
- **Spigot API**: https://hub.spigotmc.org/javadocs/spigot/
- **Paper API**: https://papermc.io/javadocs/paper/

## 💡 Need Help?

- **Build Issues**: Make sure you have Java 21+ and Maven 3.6+
- **Runtime Issues**: Check server console for ToastAPI errors
- **Questions**: Open an issue on GitHub
- **UAA Documentation**: Check the original UAA project for advanced features

---

**Note**: This is a derived work from UltimateAdvancementAPI. All legal requirements and attributions must be followed. 