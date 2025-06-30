package net.oktawia.crazyae2addons.misc;

import net.minecraft.world.level.block.Block;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import java.util.Map;

public class CradleRecipes {

    public static final String STRUCTURE_TEMPLATE = """
    {
      "symbols": {
        "A": ["%s"],
        "B": ["ae2:fluix_block"],
        "D": ["minecraft:iron_block"],
        "E": ["%s"]
      },
      "layers": [
        ["A A B A A", "A A D A A", "B D D D B", "A A D A A", "A A B A A"],
        ["A A D A A", "A E E E A", "D E E E D", "A E E E A", "A A D A A"],
        ["B D D D B", "D E E E D", "D E E E D", "D E E E D", "B D D D B"],
        ["A A D A A", "A E E E A", "D E E E D", "A E E E A", "A A D A A"],
        ["A A B A A", "A A D A A", "B D D D B", "A A D A A", "A A B A A"]
      ]
    }
    """;

    public static final Map<String, Block> RECIPES = Map.ofEntries(
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:dense_energy_cell", "crazyae2addons:energy_storage_256k"),
                    CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_256K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:energy_cell", "ae2:256k_crafting_storage"),
                    CrazyBlockRegistrar.ENERGY_STORAGE_256K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:dense_energy_cell", "crazyae2addons:energy_storage_64k"),
                    CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_64K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:energy_cell", "ae2:64k_crafting_storage"),
                    CrazyBlockRegistrar.ENERGY_STORAGE_64K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:dense_energy_cell", "crazyae2addons:energy_storage_16k"),
                    CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_16K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:energy_cell", "ae2:16k_crafting_storage"),
                    CrazyBlockRegistrar.ENERGY_STORAGE_16K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:dense_energy_cell", "crazyae2addons:energy_storage_4k"),
                    CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_4K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:energy_cell", "ae2:4k_crafting_storage"),
                    CrazyBlockRegistrar.ENERGY_STORAGE_4K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:dense_energy_cell", "crazyae2addons:energy_storage_1k"),
                    CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_1K_BLOCK.get()
            ),
            Map.entry(
                    STRUCTURE_TEMPLATE.formatted("ae2:energy_cell", "ae2:1k_crafting_storage"),
                    CrazyBlockRegistrar.ENERGY_STORAGE_1K_BLOCK.get()
            ),
            Map.entry(
                    """
                    {
                      "symbols": {
                        "A": ["ae2:fluix_block"],
                        "B": ["minecraft:obsidian"],
                        "D": ["minecraft:iron_block"],
                        "E": ["crazyae2addons:super_singularity_block"]
                      },
                      "layers": [
                        ["A A A A A", "A B B B A", "A B B B A", "A B B B A", "A A A A A"],
                        ["A B B B A", "B D D D B", "B D D D B", "B D D D B", "A B B B A"],
                        ["A B B B A", "B D D D B", "B D E D B", "B D D D B", "A B B B A"],
                        ["A B B B A", "B D D D B", "B D D D B", "B D D D B", "A B B B A"],
                        ["A A A A A", "A B B B A", "A B B B A", "A B B B A", "A A A A A"]
                      ]
                    }
                    """,
                    CrazyBlockRegistrar.PENROSE_FRAME.get()
            )
    );
}
