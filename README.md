# Crazy AE2 Addons

Crazy AE2 Addons is a Minecraft mod that enhances Applied Energistics 2 by introducing additional features to improve automation and overall gameplay experience. Built on Java 17 and Forge, it is currently designed for Minecraft version 1.20.1.

If you want to support me creating, consider downloading from [Modrinth](https://modrinth.com/mod/crazy-ae2-addons) or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/crazy-ae2-addons)

# Features
## Pattern & Crafting Tools
- [x] **Crazy Pattern Modifier** - Adjusts processing patterns, including NBT ignore settings and programmed circuit values.
- [x] **Crazy Pattern Multiplier** - Performs mathematical operations (e.g. multiplying, dividing, scaling) on processing patterns.
- [x] **Crazy Emitter Multiplier** - The same as above but for level emitters.
- [x] **Impulsed Pattern Provider** - Sends items for processing also in response to redstone pulses; ideal for probability-based recipes.
- [x] **Signalling Interface** - Emits redstone signals based on item filters, fuzzy matching, or upgrades (Redstone/Inverter cards).
- [x] **Pattern Provider GT integration** - Integrates with GregTech to set machine circuits automatically based on pattern data.
- [x] **Pattern NBT Ignore Option** - Allows processing patterns to ignore NBT data on returned/crafted items.

## Utility & Automation 
- [x] **Crafting Canceller** - Detects stalled crafts and cancels them, then automatically resubmits the tasks.
- [x] **Round Robin Item P2P Tunnel** - Evenly distributes incoming item stacks across multiple outputs.
- [x] **Chunked Fluid P2P Tunnel** - Distributes fluids in equaly sized packets, which size is configurable.
- [x] **Right Click Provider** - Continuously right-clicks in front of it. Abble of using any items in its inventory (e.g., placing blocks).
- [x] **Auto Enchanter** - Automates enchanting using XP shards and lapis, with Apotheosis integration.
- [x] **Ejector** - Export selected items from your system on a redstone pulse.
- [x] **Calculator** - Evaluates math expresions in game.

## Energy 
- [x] **Energy Exporter** - Exports FE (or EU with a GregTech battery installed) to adjacent blocks; speed up with acceleration cards.
- [x] **Ampere Meter** - Monitors FE/EU throughput between two faces, works also like a diode.

## Data & Logic System
- [x] **ME Data Controller Block** - Stores variables for the entire AE2 network, capacity depends on installed storage components. Variables cant exist without this block.
- [x] **Data Processor** - Performs instant arithmetic and logic on integer variables in the network (ADD, SUB, etc.).
- [x] **Isolated Data Processor** - Processes one logic card per tick; enables clock-like or sequential logic loops.
- [x] **Data Extractor** - Reads machine-specific data (e.g., furnace progress) and exposes it as a variable in the AE2 network.
- [x] **Data Tracker** - Outputs a redstone signal if a chosen network variable’s value exceeds zero.
- [x] **Display** - Acts as a sign or screen to display desired text and/or network variable values.

## Mob Storage
- [x] **AE2 Mob Storage** - Adds advanced mob handling and integration with AE2.
- [x] **Mob Annihilation/Formation Plane, Mob Export Bus** - A way to import/export mobs from/to ME network storage.
- [x] **Mob Farm Multiblock** - A full blown mob grinder, all inside your ME system.
- [x] **Spawner Extractor Multiblock** -  A full blown spawner, all inside your ME system.

---


### Contributing
Any contributions are welcome! Submit issues or pull requests on GitHub to improve the mod.

## License

Crazy AE2 Addons is licensed under the MIT License.

---

### [Documentation](https://github.com/GilbertzRivi/CrazyAE2Addons/wiki)
