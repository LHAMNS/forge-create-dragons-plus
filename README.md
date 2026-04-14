# Forge-Create: Dragons Plus

A Forge 1.20.1 port of [Create: Dragons Plus](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus), originally developed for NeoForge 1.21.1 by **DragonsPlusMinecraft**.

## Acknowledgements

Thanks to the following contributors for reporting issues and submitting fixes:

- **[@baiyin1223](https://github.com/baiyin1223)** - Discovered and fixed JEI recipe display failure and translation key errors ([#2](https://github.com/LHAMNS/forge-create-dragons-plus/issues/2), [#3](https://github.com/LHAMNS/forge-create-dragons-plus/pull/3))
- **[@DelsinPRO](https://github.com/DelsinPRO)** - Reported startup crash with Create 6.0.8 ([#1](https://github.com/LHAMNS/forge-create-dragons-plus/issues/1))

This mod extends [Create](https://www.curseforge.com/minecraft/mc-mods/create) with bulk processing via fan mechanics, a pumpable Dragon's Breath fluid, and a Fluid Hatch block.

## Features

- **Bulk Coloring** - Dye fluids + fan to mass-dye items, wool, glass, concrete, armor, and entities (sheep, cats, wolves, shulkers)
- **Bulk Freezing** - Powder Snow + fan to mass-freeze items (packed ice, blue ice, slime balls)
- **Bulk Ending** - Dragon's Breath + fan to infuse end energy (end stone, chorus fruit, phantom membrane)
- **Bulk Sanding** - Falling sand blocks + fan for mass sandpaper polishing
- **Fluid Hatch** - A Create-style fluid container block for contraptions
- **Dragon's Breath Fluid** - Pumpable, storable Dragon's Breath with automatic brewing recipes
- **Blaze Upgrade Smithing Template** - Found in Nether loot chests
- **Config System** - Every feature can be individually toggled on/off

## Requirements

| Dependency | Version |
|------------|---------|
| Minecraft  | 1.20.1  |
| Forge      | 47.2.0+ |
| Create     | 6.0.8+  |

## Optional Compatibility

- [Create: Dreams & Desires](https://www.curseforge.com/minecraft/mc-mods/create-dreams-desires) - Disables redundant fan types when CDP provides them
- [Create: Garnished](https://www.curseforge.com/minecraft/mc-mods/create-garnished) - Mastic resin fluids work as coloring catalysts

## Installation

1. Install Forge 1.20.1 and Create 6.0.8+
2. Place `forge-create-dragons-plus-1.0.0-mc1.20.1-all.jar` in your `mods/` folder
3. Launch the game

## License

This project is licensed under **LGPL-3.0-or-later**.

- Original code: Copyright (C) 2025 DragonsPlusMinecraft
- Ported to Forge 1.20.1 under the same license
- Original project: https://github.com/DragonsPlusMinecraft/CreateDragonsPlus

## Bug Fixes Over Upstream

This port includes fixes for several bugs present in the original NeoForge version:

- Fixed Dragon's Breath recipe duplication on `/reload`
- Fixed fluid pipe consuming effect firing during simulation queries
- Fixed DnD/Garnished mixin using unsafe interface pattern (changed to abstract class)
- Fixed upstream typo in `DragondBreathLiquidBlock` class name
