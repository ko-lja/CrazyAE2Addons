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
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

public class BlockDefs {

    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    private static final Map<BlockDefinition<?>, Map.Entry<String, Map<String, Item>>> BLOCK_RECIPES = new HashMap<>();
    
    public static final BlockDefinition<CraftingCancelerBlock> CRAFTING_CANCELER_BLOCK = block(
            "Crafting Canceler",
            "crafting_canceler_block",
            CrazyBlockRegistrar.CRAFTING_CANCELER_BLOCK.get(),
            CrazyBlockRegistrar.CRAFTING_CANCELER_BLOCK_ITEM.get(),
            "TM/MU",
            () -> Map.of(
                    "T", AEParts.MONITOR.asItem(),
                    "M", AEBlocks.CRAFTING_MONITOR.asItem(),
                    "U", AEBlocks.CRAFTING_UNIT.asItem()
            )
    );

    public static final BlockDefinition<MEDataControllerBlock> ME_DATA_CONTROLLER_BLOCK = block(
            "ME Data Controller",
            "me_data_controller_block",
            CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK.get(),
            CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK_ITEM.get(),
            "SLS/LCL/SLS",
            () -> Map.of(
                    "S", AEItems.SKY_DUST.asItem(),
                    "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem(),
                    "C", AEBlocks.CONTROLLER.asItem()
            )
    );

    public static final BlockDefinition<DataProcessorBlock> DATA_PROCESSOR_BLOCK = block(
            "Data Processor",
            "data_processor_block",
            CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK.get(),
            CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK_ITEM.get(),
            " L /TST/ L ",
            () -> Map.of(
                    "S", AEBlocks.CONTROLLER.asItem(),
                    "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem(),
                    "T", AEItems.ENGINEERING_PROCESSOR.asItem()
            )
    );

    public static final BlockDefinition<DataTrackerBlock> DATA_TRACKER_BLOCK = block(
            "Data Tracker",
            "data_tracker_block",
            CrazyBlockRegistrar.DATA_TRACKER_BLOCK.get(),
            CrazyBlockRegistrar.DATA_TRACKER_BLOCK_ITEM.get(),
            "TSL",
            () -> Map.of(
                    "T", Items.REDSTONE_TORCH,
                    "S", AEBlocks.SKY_STONE_BLOCK.asItem(),
                    "L", CrazyItemRegistrar.LOGIC_CARD.get().asItem()
            )
    );

    public static final BlockDefinition<CircuitedPatternProviderBlock> CIRCUITED_PATTERN_PROVIDER_BLOCK =
            IsModLoaded.isGTCEuLoaded()
                    ? block(
                    "Circuited Pattern Provider",
                    "circuited_pp",
                    CrazyBlockRegistrar.CIRCUITED_PATTERN_PROVIDER_BLOCK.get(),
                    CrazyBlockRegistrar.CIRCUITED_PATTERN_PROVIDER_BLOCK_ITEM.get(),
                    "PCC",
                    () -> Map.of(
                            "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                            "C", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
                    )
            )
                    : null;

    public static final BlockDefinition<AmpereMeterBlock> AMPERE_METER_BLOCK = block(
            "Ampere Meter",
            "ampere_meter",
            CrazyBlockRegistrar.AMPERE_METER_BLOCK.get(),
            CrazyBlockRegistrar.AMPERE_METER_BLOCK_ITEM.get(),
            "ICE",
            () -> Map.of(
                    "I", AEParts.IMPORT_BUS.asItem(),
                    "C", DATA_TRACKER_BLOCK.asItem(),
                    "E", AEParts.EXPORT_BUS.asItem()
            )
    );

    public static final BlockDefinition<IsolatedDataProcessorBlock> ISOLATED_DATA_PROCESSOR_BLOCK = block(
            "Isolated Data Processor",
            "isolated_data_processor_block",
            CrazyBlockRegistrar.ISOLATED_DATA_PROCESSOR_BLOCK.get(),
            CrazyBlockRegistrar.ISOLATED_DATA_PROCESSOR_BLOCK_ITEM.get(),
            "WWW/WCW/WWW",
            () -> Map.of(
                    "W", net.minecraft.world.level.block.Blocks.WHITE_WOOL.asItem(),
                    "C", DATA_PROCESSOR_BLOCK.asItem()
            )
    );

    public static final BlockDefinition<ImpulsedPatternProviderBlock> IMPULSED_PATTERN_PROVIDER_BLOCK = block(
            "Impulsed Pattern Provider",
            "impulsed_pp",
            CrazyBlockRegistrar.IMPULSED_PATTERN_PROVIDER_BLOCK.get(),
            CrazyBlockRegistrar.IMPULSED_PATTERN_PROVIDER_BLOCK_ITEM.get(),
            "PDR",
            () -> Map.of(
                    "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                    "D", Items.DIAMOND,
                    "R", Items.REDSTONE_TORCH
            )
    );

    public static final BlockDefinition<SignallingInterfaceBlock> SIGNALLING_INTERFACE_BLOCK = block(
            "Signalling Interface",
            "signalling_interface_block",
            CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK.get(),
            CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK_ITEM.get(),
            "IT",
            () -> Map.of(
                    "I", AEBlocks.INTERFACE.asItem(),
                    "T", DATA_TRACKER_BLOCK.asItem()
            )
    );

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }
    public static Map<BlockDefinition<?>, Map.Entry<String, Map<String, Item>>> getBlockRecipes() {
        return BLOCK_RECIPES;
    }

    public static <T extends Block> BlockDefinition<T> block(
            String englishName,
            String id,
            T Block,
            BlockItem Item,
            String recipe,
            Supplier<Map<String, Item>> recipe_map) {
        var definition = new BlockDefinition<>(englishName, CrazyAddons.makeId(id), Block, Item);
        BLOCKS.add(definition);
        BLOCK_RECIPES.put(definition, Map.entry(recipe, recipe_map.get()));
        return definition;
    }
}