# Crazy AE2 Addons

Crazy AE2 Addons is a Minecraft mod that enhances Applied Energistics 2 by introducing additional features to improve automation and overall gameplay experience. Built on Java 17 and Forge, it is currently designed for Minecraft version 1.20.1.

## Features

- [x] **Crafting Canceller**: Automatically detects and cancels frozen crafting operations, rescheduling them for right after. Configurable delay before considering a task frozen.

- [x] **Round Robin Item P2P Tunnel**: Enables round-robin item distribution between multiple outputs, between multiple item insertions. When inputting more items, the tunnel evenly splits the stack across all outputs.

- [x] **Block Entity Tickers**: A costly but effective way to significantly speed up machines.

- [x] **NBT Export Bus**: An export bus that can export items based on their NBT tags. Supports wildcard like ``{StoredEnchantments:ANY}`` or ``{ANY:SomeValue,SomeKey:AnotherValue}`` or even ``{ANY:ANY}``.

- [x] **Processing Pattern NBT Ignore Option**: Adds a configuration option to allow processing patterns to ignore NBT data for items returned to the network. This would simplify crafting complex items, such as entangled singularities.

- [x] **Crazy Pattern Multiplier**: Item that enables players to perform mathematical operations on input/output items in processing patterns (e.g., multiplying all inputs/outputs by ``1/(3+2)`` or `3.11e4`, etc.). After clicking on any block with inventory, it will multiply all the patterns inside it too.

- [x] **Crazy Pattern Modifier**: Item that enables players to change NBT ignore setting, or programmed circuit setting for any given processing pattern.

- [x] **Impulsed Pattern Provider**: Implements crafting functionality for recipes with a success probability. After sending the to some adapter, and before the primary crafting result is returned, it will repeat sending stacks out on each redstone pulse recieved.

- [x] **Signalling Interface**: If upgraded with redstone card, sends redstone pulse on item insertion, if the item matches any set filter, or if it does not when upgraded with inverter card. Works with fuzzy card.

- [x] **Display**: A fancy sign, thats also capable of displaying any variable stored inside the network. 

- [x] **Data Extractors**: Device that provide information about monitored machine and make that data accessible to the ME network.

- [x] **Right-Click Provider**: Cable part that is constantly right clicking the position in front of it, with any item it holds. (Places blocks, lights fiers even opens/closes doors)
 
- [ ] **Enchanting Table Automation**: Automates enchanting by enabling the input of lapis, XP shards, and items. you can select an enchantment tier (cheap, medium, or expensive) and it will automatically enchant items. The enchantment power depends on the amount of books ardound the enchanting table, also works with apotheosis. (Had to dissable it for a moment, it will be back together with mob related features)

- [ ] **AE2 Mob Storage**: Add mob-related functionality, including a Mob Import/Export Bus for mob integration or even mob farms, fully integrated into the AE2 network.

- [ ] **Ultimate P2P Tunnel**: Create a powerful P2P tunnel, akin to a wormhole, designed in true AE2 fashion.

- [ ] **Demand Prediction**: Analyze resource trends in the network to predict future demand and trigger events based on those predictions.

- [ ] **Suggestions/Recommendations**: Analyze resource usage trends and recommend actions to the player. For example, suggest increasing power production if a power shortage is predicted.

- [ ] **Custom Goal Registering/Monitoring**: Allow players to set goals, such as storing a specific amount of a resource, and track progress. This is primarily a visualization tool but can trigger events based on conditions.

- [ ] **ETA Calculations**: Add a service capable of calculating the estimated time for specific tasks and providing that data to other systems or triggering custom events.

- [ ] **Scripting Mods Integration**: Add robust integration with scripting-capable mods (e.g., SFM, OpenComputers, or ComputerCraft). This feature could be developed as a separate mod in a different repository. For example, add a database block providing a database interface for scripts, with data stored inside AE2 cells.

- [ ] **ME Network Synchronization Across Instances**: Enable synchronization of ME networks between server instances on a local network. This could support multi-threaded server setups where different dimensions run on separate threads. Feasibility and implementation timeline remain uncertain.

---

### Contributing
Any contributions are welcome! Submit issues or pull requests on GitHub to improve the mod.

## License

Crazy AE2 Addons is licensed under the MIT License.

---

### Documentation

Imagine that here is a link to the wiki page containing all the documentation.
