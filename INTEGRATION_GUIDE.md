# 🔌 ToastAPI Integration Guide

A complete guide for developers who want to integrate ToastAPI into their Minecraft plugins.

## 📋 Table of Contents
- [Setup](#setup)
- [Basic Usage](#basic-usage)
- [Advanced Usage](#advanced-usage)
- [Distribution Options](#distribution-options)
- [Legal Considerations](#legal-considerations)

## 🛠️ Setup

### 1. **Add ToastAPI Dependency**

#### Option A: Local Installation (Recommended for Testing)
```bash
# Download ToastAPI-1.0.0.jar, then install to local Maven repo:
mvn install:install-file \
  -Dfile=ToastAPI-1.0.0.jar \
  -DgroupId=com.frengor \
  -DartifactId=toastapi \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

#### Option B: Use JitPack (RECOMMENDED)
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

### 2. **Maven Dependencies (pom.xml)**
```xml
<dependencies>
    <!-- ToastAPI -->
    <dependency>
        <groupId>com.frengor</groupId>
        <artifactId>toastapi</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Spigot/Paper API -->
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.21.3-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 3. **Plugin Dependencies (plugin.yml)**
```yaml
name: YourPlugin
version: 1.0.0
main: com.yourname.yourplugin.Main
api-version: 1.21
depend: [ToastAPI]
# or soft-depend: [ToastAPI] if optional
```

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
        // Check if ToastAPI is available
        if (!getServer().getPluginManager().isPluginEnabled("ToastAPI")) {
            getLogger().warning("ToastAPI not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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

### **Option 1: Local Installation**
Users must install ToastAPI to their local Maven repository:
```bash
mvn install:install-file \
  -Dfile=ToastAPI-1.0.0.jar \
  -DgroupId=com.frengor \
  -DartifactId=toastapi \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

### **Option 2: JitPack (GitHub)**
If you host ToastAPI on GitHub, others can use JitPack:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### **Option 3: Shade into Plugin**
Include ToastAPI directly in your plugin JAR:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
    <configuration>
        <relocations>
            <relocation>
                <pattern>com.frengor.toastapi</pattern>
                <shadedPattern>com.yourname.yourplugin.libs.toastapi</shadedPattern>
            </relocation>
        </relocations>
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

### **Option 4: Maven Repository**
Host on a public Maven repository like Maven Central or your own Nexus.

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
- **GPL-3.0 License**: https://www.gnu.org/licenses/gpl-3.0.html
- **Spigot API**: https://hub.spigotmc.org/javadocs/spigot/
- **Paper API**: https://papermc.io/javadocs/paper/

## 💡 Need Help?

- Open an issue on GitHub
- Check the original UAA documentation
- Join the community Discord (if available)

---

**Note**: This is a derived work from UltimateAdvancementAPI. All legal requirements and attributions must be followed. 