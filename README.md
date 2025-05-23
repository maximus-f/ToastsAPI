# UltimateAdvancementAPI
[![Build Status main Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/badge/icon?subject=main&style=flat)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/)
[![Build Status dev Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/badge/icon?subject=dev&style=flat)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/)
[![License](https://img.shields.io/badge/license-LGPL--3.0-orange?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/blob/main/LGPL)
[![Version](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.github.com%2Frepos%2Ffrengor%2FUltimateAdvancementAPI%2Ftags&query=%24%5B%3A1%5D.name&style=flat&label=version&color=blue)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/)
[![Issues](https://img.shields.io/github/issues/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/issues)
[![Stars](https://img.shields.io/github/stars/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/stargazers)
[![Forks](https://img.shields.io/github/forks/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/network)
[![Contributors](https://img.shields.io/github/contributors/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/graphs/contributors)

A powerful API to create custom advancements for your minecraft server.

![Advancement Tab Image](https://github.com/frengor/UltimateAdvancementAPI/wiki/images/spigot-photo.png)

**Modrinth Page:** <https://modrinth.com/plugin/ultimateadvancementapi>  
**Spigot Page:** <https://www.spigotmc.org/resources/95585/>  
**Hangar Page:** <https://hangar.papermc.io/DevHeim/UltimateAdvancementAPI>  
**UltimateAdvancementGenerator:** <https://escanortargaryen.dev/UltimateAdvancementGenerator/>  
**Discord:** <https://discord.gg/BMg6VJk5n3>  
**Official Wiki:** <https://github.com/frengor/UltimateAdvancementAPI/wiki/>  
**Javadoc:** <https://frengor.com/javadocs/UltimateAdvancementAPI/latest/>  
**Jenkins:** <https://jenkins.frengor.com/job/UltimateAdvancementAPI/>

**Get it with maven:**
```xml
<repositories>
    <repository>
        <id>fren_gor</id>
        <url>https://nexus.frengor.com/repository/public/</url>
    </repository>
</repositories>
```   
```xml
<dependency>
    <groupId>com.frengor</groupId>
    <artifactId>ultimateadvancementapi</artifactId>
    <version>2.5.1</version>
    <scope>provided</scope>
</dependency>
```

#### Example Plugin:

An example of plugin using UltimateAdvancementAPI can be found [here](https://github.com/DevHeim-space/UltimateAdvancementAPI-Showcase).

More examples by the community can be found in the `showcase` forum on [Discord](https://discord.gg/BMg6VJk5n3).

#### Test Plugin:

The plugin used for tests can be found [here](https://github.com/frengor/UltimateAdvancementAPI-Tests).

## Contributing

Feel free to open issues or pull requests. Feature requests can be done opening an issue, the `enhancement` tag will be applied by maintainers.

For pull requests, open them towards the `dev` branch, as the `main` branch is only for releases. Make sure to allow edits by maintainers.
Also, please use the formatting style settings present under `.idea/codeStyles` folder.

## Required Java version

Currently, the project is compiled for Java 16, although the minimum required Java version might change in future releases.

> We consider changing the minimum required Java version a breaking change, so DO NOT expect it to be frequently modified.

In order to compile the code you must be using (at least) the Java version required by the last Minecraft version, since the project uses NMS.

## License

This project is licensed under the [GNU Lesser General Public License v3.0 or later](https://www.gnu.org/licenses/lgpl-3.0.txt).

## Credits

UltimateAdvancementAPI has been made by [fren_gor](https://github.com/frengor) and [EscanorTargaryen](https://github.com/EscanorTargaryen).  
The API uses the following libraries:
  * [EventManagerAPI](https://github.com/frengor/EventManagerAPI) (released under Apache-2.0 license) to handle events
  * [Libby](https://github.com/AlessioDP/libby) (released under MIT license) to handle dependencies at runtime
  * [CommandAPI](https://github.com/JorelAli/CommandAPI) (released under MIT license) to add commands to the plugin version of the API
  * [HikariCP](https://github.com/brettwooldridge/HikariCP) (released under Apache-2.0 license) to connect to MySQL databases
  * [bStats](https://bstats.org/) (the Java library is released under MIT license) to collect usage data (which can be found [here](https://bstats.org/plugin/bukkit/UltimateAdvancementAPI/12593)) about the plugin version of the API

# ToastAPI

A lightweight toast notification API for Minecraft servers (1.20.x supported).

## What It Does

ToastAPI allows you to send custom toast notifications (those advancement popup messages) to players without them staying in the advancement menu. Perfect for temporary notifications, welcome messages, and alerts.

## Usage

### As a Standalone Plugin

1. **Download** the `ToastAPI-1.0.0.jar` from `ToastAPI/target/`
2. **Install** it in your server's `plugins/` folder
3. **Restart** your server

#### Commands

- `/toast <type> [message]` - Send a test toast notification
  - Types: `task`, `goal`, `challenge`
  - Example: `/toast goal Welcome to our server!`
  - Permission: `toastapi.toast` (default: op)

#### Features

- **Welcome Toast**: Players automatically receive a welcome toast when they join
- **Test Command**: Use `/toast` to test different toast types
- **Different Frame Types**:
  - `task` - Regular square frame (grass block icon)
  - `goal` - Fancy frame with rounded corners (diamond icon)  
  - `challenge` - Ornate frame with decorative elements (dragon head icon)

### As a Library (For Developers)

Add ToastAPI as a dependency to your plugin:

```xml
<dependency>
    <groupId>com.frengor</groupId>
    <artifactId>toastapi</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

Then shade it into your plugin and use the API:

```java
import com.frengor.toastapi.ToastAPI;
import com.frengor.toastapi.AdvancementFrameType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

// Send a toast notification
ToastAPI.displayCustomToast(
    player, 
    new ItemStack(Material.DIAMOND), 
    "§6Achievement Unlocked!", 
    AdvancementFrameType.GOAL
);
```

## Supported Versions

- **Currently**: Minecraft 1.20.x (Paper/Spigot)
- **Planned**: Full support for 1.15-1.21

## How It Works

1. Creates temporary advancement entries
2. Sends advancement packets to trigger the toast display
3. Immediately removes the advancements so they don't clutter the advancement menu
4. Uses NMS (Net Minecraft Server) for cross-version compatibility

## Development

This project uses Maven and includes NMS wrappers for different Minecraft versions.

```bash
# Build the plugin
mvn clean package

# The plugin JAR will be in ToastAPI/target/ToastAPI-1.0.0.jar
```

## License

GNU Lesser General Public License v3.0 or later
