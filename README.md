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

## Attribution
This project contains code derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, licensed under **GPL v3**. The NMS wrapper system and advancement handling code are based on this original work.

**Original UAA Authors:** fren_gor, EscanorTargaryen  
**Original Project:** https://github.com/frengor/UltimateAdvancementAPI  
**Original License:** GPL-3.0

## License
This project is licensed under the **GNU General Public License v3.0** (same as the original UAA project).

See `LICENSE` file for the full GPL v3 license terms.

## 🚨 GPL Notice for Developers
Since this plugin is GPL-licensed, any plugins that use ToastAPI as a dependency may also need to be GPL-licensed due to the "copyleft" nature of GPL. Please consult with a legal expert if you have concerns about GPL compatibility with your project.
