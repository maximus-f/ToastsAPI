# ToastAPI

A lightweight Minecraft plugin for displaying toast notifications across versions 1.20.2, 1.20.4, 1.21, 1.21.4, 1.21.5

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
This project contains code derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor, licensed under LGPL v3. The NMS wrapper system and advancement handling code are based on this original work.

## License
- **LGPL v3 portions**: NMS wrappers and advancement system (from UltimateAdvancementAPI)
- **Plugin code**: Licensed under your chosen license

See `LGPL` file for the LGPL v3 license terms.
