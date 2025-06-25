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
                CrazyBlockRegistrar.MOB_FARM_INPUT.get(),
                "WWW/WEW/WWW",
                () -> Map.of(
                        "W", CrazyBlockRegistrar.MOB_FARM_WALL.get().asItem(),
                        "E", CrazyItemRegistrar.MOB_EXPORT_BUS.get()
                )
        );

        block(
                CrazyBlockRegistrar.MOB_FARM_COLLECTOR.get(),
                "WHW/HEH/WHW",
                () -> Map.of(
                        "W", CrazyBlockRegistrar.MOB_FARM_WALL.get().asItem(),
                        "H", AEParts.IMPORT_BUS.asItem(),
                        "E", AEItems.FLUIX_PEARL.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.MOB_FARM_DAMAGE.get(),
                "DND/NEN/DND",
                () -> Map.of(
                        "D", AEBlocks.DENSE_ENERGY_CELL.asItem(),
                        "N", Items.NETHERITE_INGOT,
                        "E", Items.ECHO_SHARD
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

        block(
                CrazyBlockRegistrar.MOB_FARM_CONTROLLER.get(),
                "WE",
                () -> Map.of(
                        "W", CrazyBlockRegistrar.MOB_FARM_WALL.get().asItem(),
                        "E", Items.NETHER_STAR
                )
        );

        block(
                CrazyBlockRegistrar.CRAFTING_SCHEDULER_BLOCK.get(),
                "PRE",
                () -> Map.of(
                        "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "R", Items.REDSTONE,
                        "E", AEParts.LEVEL_EMITTER.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.REINFORCED_MATTER_CONDENSER_BLOCK.get(),
                "IPI/GMG/ICI",
                () -> Map.of(
                        "I", Items.IRON_INGOT,
                        "P", Blocks.IRON_BLOCK.asItem(),
                        "G", AEBlocks.QUARTZ_GLASS.asItem(),
                        "M", AEBlocks.CONDENSER.asItem(),
                        "C", AEItems.CELL_COMPONENT_256K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.PENROSE_FRAME.get(),
                "ABA/CDC/ABA",
                () -> Map.of(
                        "A", AEBlocks.FLUIX_BLOCK.asItem(),
                        "B", Blocks.IRON_BLOCK.asItem(),
                        "C", Items.DIAMOND,
                        "D", CrazyItemRegistrar.SUPER_SINGULARITY.get()
                )
        );

        block(
                CrazyBlockRegistrar.PENROSE_COIL.get(),
                "AAA/ADA/AAA",
                () -> Map.of(
                        "A", Blocks.COPPER_BLOCK.asItem(),
                        "D", CrazyBlockRegistrar.PENROSE_FRAME.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.PENROSE_CONTROLLER.get(),
                "AAA/ANA/AAA",
                () -> Map.of(
                        "A", CrazyBlockRegistrar.PENROSE_FRAME.get().asItem(),
                        "N", Items.NETHER_STAR
                )
        );

        block(
                CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get(),
                "AAA/APA/AAA",
                () -> Map.of(
                        "A", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "P", AEItems.ENGINEERING_PROCESSOR.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_FRAME_BLOCK.get(),
                "FMF/MEM/FMF",
                () -> Map.of(
                        "F", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "E", AEBlocks.ENERGY_CELL.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_CONTROLLER_BLOCK.get(),
                "FFF/FNF/FFF",
                () -> Map.of(
                        "F", CrazyBlockRegistrar.ENERGY_STORAGE_FRAME_BLOCK.get().asItem(),
                        "N", Items.NETHER_STAR
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_1K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "M", AEItems.MATTER_BALL.asItem(),
                        "S", AEItems.SINGULARITY.asItem(),
                        "K", AEItems.CELL_COMPONENT_1K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_4K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "M", AEItems.MATTER_BALL.asItem(),
                        "S", AEItems.SINGULARITY.asItem(),
                        "K", AEItems.CELL_COMPONENT_4K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_16K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "M", AEItems.MATTER_BALL.asItem(),
                        "S", AEItems.SINGULARITY.asItem(),
                        "K", AEItems.CELL_COMPONENT_16K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_64K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "M", AEItems.MATTER_BALL.asItem(),
                        "S", AEItems.SINGULARITY.asItem(),
                        "K", AEItems.CELL_COMPONENT_64K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.ENERGY_STORAGE_256K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        "M", AEItems.MATTER_BALL.asItem(),
                        "S", AEItems.SINGULARITY.asItem(),
                        "K", AEItems.CELL_COMPONENT_256K.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_1K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.SINGULARITY.asItem(),
                        "S", CrazyItemRegistrar.SUPER_SINGULARITY.get().asItem(),
                        "K", CrazyBlockRegistrar.ENERGY_STORAGE_1K_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_4K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.SINGULARITY.asItem(),
                        "S", CrazyItemRegistrar.SUPER_SINGULARITY.get().asItem(),
                        "K", CrazyBlockRegistrar.ENERGY_STORAGE_4K_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_16K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.SINGULARITY.asItem(),
                        "S", CrazyItemRegistrar.SUPER_SINGULARITY.get().asItem(),
                        "K", CrazyBlockRegistrar.ENERGY_STORAGE_16K_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_64K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.SINGULARITY.asItem(),
                        "S", CrazyItemRegistrar.SUPER_SINGULARITY.get().asItem(),
                        "K", CrazyBlockRegistrar.ENERGY_STORAGE_64K_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_256K_BLOCK.get(),
                "CMC/MKM/CSC",
                () -> Map.of(
                        "C", AEItems.FLUIX_CRYSTAL.asItem(),
                        "M", AEItems.SINGULARITY.asItem(),
                        "S", CrazyItemRegistrar.SUPER_SINGULARITY.get().asItem(),
                        "K", CrazyBlockRegistrar.ENERGY_STORAGE_256K_BLOCK.get().asItem()
                )
        );

        block(
                CrazyBlockRegistrar.AUTO_BUILDER_BLOCK.get(),
                "EPE/BRN/EPE",
                () -> Map.of(
                        "E", Items.EMERALD.asItem(),
                        "P", CrazyItemRegistrar.BUILDER_PATTERN.get().asItem(),
                        "B", AEParts.IMPORT_BUS.asItem(),
                        "R", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "N", AEParts.EXPORT_BUS.asItem()
                )
        );

        block(
                CrazyBlockRegistrar.DATA_SETTER_BLOCK.get(),
                "TL",
                () -> Map.of(
                        "T", CrazyBlockRegistrar.DATA_TRACKER_BLOCK.get().asItem(),
                        "L", Items.REDSTONE_TORCH.asItem()
                )
        );
    }

}