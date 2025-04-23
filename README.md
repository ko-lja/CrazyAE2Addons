# Crazy AE2 Addons

Crazy AE2 Addons is a Minecraft mod that enhances Applied Energistics 2 by introducing additional features to improve automation and overall gameplay experience. Built on Java 17 and Forge, it is currently designed for Minecraft version 1.20.1.

If you want to support me creating, consider downloading from [Modrinth](https://modrinth.com/mod/crazy-ae2-addons) or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/crazy-ae2-addons)

# Features
## Pattern & Crafting Tools
- [x] **Crazy Pattern Modifier** – Adjusts processing patterns, including NBT ignore settings and programmed circuit values.
- [x] **Crazy Pattern Multiplier** – Performs mathematical operations (e.g. multiplying, dividing, scaling) on processing patterns.
- [x] **Impulsed Pattern Provider** – Sends items for processing also in response to redstone pulses; ideal for probability-based recipes.
- [x] **Signalling Interface** – Emits redstone signals based on item filters, fuzzy matching, or upgrades (Redstone/Inverter cards).
- [x] **Circuited Pattern Provider** – Integrates with GregTech to set machine circuits automatically based on pattern data.
- [x] **Pattern NBT Ignore Option** – Allows processing patterns to ignore NBT data on returned/crafted items.

## Utility & Automation Blocks
- [x] **Crafting Canceller** – Detects stalled crafts and cancels them, then automatically resubmits the tasks.
- [x] **Round Robin Item P2P Tunnel** – Evenly distributes incoming item stacks across multiple outputs.
- [x] **Chunked Fluid P2P Tunnel** – Distributes fluids in equaly sized packets, which size is configurable.
- [x] **Right Click Provider** – Continuously right-clicks in front of it. Abble of using any items in its inventory (e.g., placing blocks).

## Energy & Redstone
- [x] **Energy Exporter** – Exports FE (or EU with a GregTech battery installed) to adjacent blocks; speed up with acceleration cards.
- [x] **Ampere Meter** – Monitors FE/EU throughput between two faces, works also like a diode.

## Data & Logic System
- [x] **ME Data Controller Block** – Stores variables for the entire AE2 network, capacity depends on installed storage components. Variables cant exist without this block.
- [x] **Data Processor** – Performs instant arithmetic and logic on integer variables in the network (ADD, SUB, etc.).
- [x] **Isolated Data Processor** – Processes one logic card per tick; enables clock-like or sequential logic loops.
- [x] **Data Extractor** – Reads machine-specific data (e.g., furnace progress) and exposes it as a variable in the AE2 network.
- [x] **Data Tracker** – Outputs a redstone signal if a chosen network variable’s value exceeds zero.
- [x] **Display** – Acts as a sign or screen to display desired text and/or network variable values.

---

## Additional Planned / Unfinished Ideas
- [ ] **Auto Enchanter** – Automates enchanting using XP shards and lapis, supporting tier-based costs and Apotheosis integration.
- [ ] **AE2 Mob Storage** – Adds advanced mob handling and integration with AE2 (import/export, storage).
- [ ] **Ultimate P2P Tunnel** – A next-generation P2P tunnel for large-scale or cross-dimensional routing.
- [ ] **Demand Prediction** – Monitors resource usage to predict future needs and potentially automate expansions.
- [ ] **Suggestions/Recommendations** – Observes resource trends to offer advice (e.g., adding more power generation).
- [ ] **Goal Registering/Monitoring** – Allows players to define specific resource/production goals and track them in the network.
- [ ] **ETA Calculations** – Computes time estimates for ongoing tasks and shares that data with other systems or triggers.
- [ ] **Scripting Integration** – Connects to external scripting mods (like ComputerCraft or OpenComputers) for full automation.

---


### Contributing
Any contributions are welcome! Submit issues or pull requests on GitHub to improve the mod.

## License

Crazy AE2 Addons is licensed under the MIT License.

---

### [Documentation](https://github.com/GilbertzRivi/CrazyAE2Addons/wiki)
