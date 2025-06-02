package net.oktawia.crazyae2addons.defs;

import java.util.*;
import java.util.function.Supplier;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

public class BlockDefs {

    private static final Map<Block, Map.Entry<String, Map<String, Item>>> BLOCK_RECIPES = new HashMap<>();

    public static Map<Block, Map.Entry<String, Map<String, Item>>> getBlockRecipes() {
        return BLOCK_RECIPES;
    }

    public static void block(
            Block block,
            String recipe,
            Supplier<Map<String, Item>> recipe_map) {
        BLOCK_RECIPES.put(block, Map.entry(recipe, recipe_map.get()));
    }

    public static void registerRecipes(){
        block(
                CrazyBlockRegistrar.CRAFTING_CANCELER_BLOCK.get(),
                "TM/MU",
                () -> Map.of(
                        "T", AEParts.MONITOR.asItem(),
                        "M", AEBlocks.CRAFTING_MONITOR.asItem(),
                        "U", AEBlocks.CRAFTING_UNIT.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK.get(),
                "SLS/LCL/SLS",
                () -> Map.of(
                        "S", AEItems.SKY_DUST.asItem(),
                        "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem(),
                        "C", AEBlocks.CONTROLLER.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK.get(),
                " L /TST/ L ",
                () -> Map.of(
                        "S", AEBlocks.CONTROLLER.asItem(),
                        "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem(),
                        "T", AEItems.ENGINEERING_PROCESSOR.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DATA_TRACKER_BLOCK.get(),
                "TSL",
                () -> Map.of(
                        "T", Items.REDSTONE_TORCH,
                        "S", AEBlocks.SKY_STONE_BLOCK.asItem(),
                        "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.AMPERE_METER_BLOCK.get(),
                "ICE",
                () -> Map.of(
                        "I", AEParts.IMPORT_BUS.asItem(),
                        "C", CrazyBlockRegistrar.DATA_TRACKER_BLOCK.get().asItem(),
                        "E", AEParts.EXPORT_BUS.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ISOLATED_DATA_PROCESSOR_BLOCK.get(),
                "WWW/WCW/WWW",
                () -> Map.of(
                        "W", net.minecraft.world.level.block.Blocks.WHITE_WOOL.asItem(),
                        "C", CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.IMPULSED_PATTERN_PROVIDER_BLOCK.get(),
                "PDR",
                () -> Map.of(
                        "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "D", Items.DIAMOND,
                        "R", Items.REDSTONE_TORCH
                )
        );

        block(
                CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK.get(),
                "IT",
                () -> Map.of(
                        "I", AEBlocks.INTERFACE.asItem(),
                        "T", CrazyBlockRegistrar.DATA_TRACKER_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.AUTO_ENCHANTER_BLOCK.get(),
                " S /ICO/BEB",
                () -> Map.of(
                        "S", Items.NETHER_STAR,
                        "I", AEParts.IMPORT_BUS.asItem(),
                        "C", Blocks.ENCHANTING_TABLE.asItem(),
                        "O", AEParts.EXPORT_BUS.asItem(),
                        "B", Blocks.OBSIDIAN.asItem(),
                        "E", AEBlocks.DENSE_ENERGY_CELL.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.EJECTOR_BLOCK.get(),
                "PR",
                () -> Map.of(
                        "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "R", Items.REDSTONE.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.MOB_FARM_WALL.get(),
                "BIB/IRI/BIB",
                () -> Map.of(
                        "I", Blocks.IRON_BLOCK.asItem(),
                        "B", Blocks.IRON_BARS.asItem(),
                        "R", Items.ROTTEN_FLESH
                )
        );

        block(
                CrazyBlockRegistrar.SPAWNER_EXTRACTOR_WALL.get(),
                "WEW/ESE/WEW",
                () -> Map.of(
                        "W", CrazyBlockRegistrar.MOB_FARM_WALL.get().asItem(),
                        "E", Items.BLAZE_ROD,
                        "S", AEItems.FLUIX_PEARL.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.SPAWNER_EXTRACTOR_CONTROLLER.get(),
                "WE",
                () -> Map.of(
                        "W", CrazyBlockRegistrar.SPAWNER_EXTRACTOR_WALL.get().asItem(),
                        "E", Items.NETHER_STAR
                )
        );
    }

}