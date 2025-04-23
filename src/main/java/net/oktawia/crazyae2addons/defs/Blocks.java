package net.oktawia.crazyae2addons.defs;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.items.*;
import net.oktawia.crazyae2addons.CrazyAddons;

public class Blocks {

    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    private static final Map<BlockDefinition<?>, Map.Entry<String, Map<String, Item>>> BLOCK_RECIPES = new HashMap<>();
    public static final BlockDefinition<CraftingCancelerBlock> CRAFTING_CANCELER_BLOCK = block(
            "Crafting Canceler",
            "crafting_canceler_block",
            CraftingCancelerBlock::new,
            CraftingCancelerBlockItem::new,
            "TM/MU",
            Map.of(
                    "T", AEParts.MONITOR.asItem(),
                    "M", AEBlocks.CRAFTING_MONITOR.asItem(),
                    "U", AEBlocks.CRAFTING_UNIT.asItem()
            )

    );
    public static final BlockDefinition<AutoEnchanterBlock> AUTO_ENCHANTER_BLOCK = block(
            "Auto Enchanter",
            "auto_enchanter_block",
            AutoEnchanterBlock::new,
            AutoEnchanterBlockItem::new,
            " D /EUI/MMM",
            Map.of(
                    "D", Items.DIAMOND,
                    "E", AEParts.EXPORT_BUS.asItem(),
                    "U", net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE.asItem(),
                    "I", AEParts.IMPORT_BUS.asItem(),
                    "M", net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()
            )
    );
    public static final BlockDefinition<MEDataControllerBlock> ME_DATA_CONTROLLER_BLOCK = block(
            "ME Data Controller",
            "me_data_controller_block",
            MEDataControllerBlock::new,
            MEDataControllerBlockItem::new,
            "SLS/LCL/SLS",
            Map.of(
                    "S", AEItems.SKY_DUST.asItem(),
                    "L", net.oktawia.crazyae2addons.defs.Items.LOGIC_CARD.asItem(),
                    "C", AEBlocks.CONTROLLER.asItem()
            )
    );
    public static final BlockDefinition<DataProcessorBlock> DATA_PROCESSOR_BLOCK = block(
            "Data Processor",
            "data_processor_block",
            DataProcessorBlock::new,
            DataProcessorBlockItem::new,
            " L /TST/ L ",
            Map.of(
                    "S", AEBlocks.CONTROLLER.asItem(),
                    "L", net.oktawia.crazyae2addons.defs.Items.LOGIC_CARD.asItem(),
                    "T", AEItems.ENGINEERING_PROCESSOR.asItem()
            )
    );
    public static final BlockDefinition<DataTrackerBlock> DATA_TRACKER_BLOCK = block(
            "Data Tracker",
            "data_tracker_block",
            DataTrackerBlock::new,
            DataTrackerBlockItem::new,
            "TSL",
            Map.of(
                    "T", Items.REDSTONE_TORCH,
                    "S", AEBlocks.SKY_STONE_BLOCK.asItem(),
                    "L", net.oktawia.crazyae2addons.defs.Items.LOGIC_CARD.asItem()
            )
    );
    public static final BlockDefinition<CircuitedPatternProviderBlock> CIRCUITED_PATTERN_PROVIDER_BLOCK = block(
            "Circuited Pattern Provider",
            "circuited_pp",
            CircuitedPatternProviderBlock::new,
            CircuitedPatternProviderBlockItem::new,
            "PCC",
            Map.of(
                    "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                    "C", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
            )
    );
    public static final BlockDefinition<AmpereMeterBlock> AMPERE_METER_BLOCK = block(
            "Ampere Meter",
            "ampere_meter",
            AmpereMeterBlock::new,
            AmpereMeterBlockItem::new,
            "ICE",
            Map.of(
                    "I", AEParts.IMPORT_BUS.asItem(),
                    "C", DATA_TRACKER_BLOCK.asItem(),
                    "E", AEParts.EXPORT_BUS.asItem()
            )
    );
    public static final BlockDefinition<IsolatedDataProcessorBlock> ISOLATED_DATA_PROCESSOR_BLOCK = block(
            "Isolated Data Processor",
            "isolated_data_processor_block",
            IsolatedDataProcessorBlock::new,
            IsolatedDataProcessorBlockItem::new,
            "WWW/WCW/WWW",
            Map.of(
                    "W", net.minecraft.world.level.block.Blocks.WHITE_WOOL.asItem(),
                    "C", DATA_PROCESSOR_BLOCK.asItem()
            )
    );
    public static final BlockDefinition<ImpulsedPatternProviderBlock> IMPULSED_PATTERN_PROVIDER_BLOCK = block(
            "Impulsed Pattern Provider",
            "impulsed_pp",
            ImpulsedPatternProviderBlock::new,
            ImpulsedPatternProviderBlockItem::new,
            "PDR",
            Map.of(
                    "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                    "D", Items.DIAMOND,
                    "R", Items.REDSTONE_TORCH
            )
    );
    public static final BlockDefinition<SignallingInterfaceBlock> SIGNALLING_INTERFACE_BLOCK = block(
            "Signalling Interface",
            "signalling_interface_block",
            SignallingInterfaceBlock::new,
            SignallingInterfaceBlockItem::new,
            "IT",
            Map.of(
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
            Supplier<T> blockSupplier,
            BiFunction<Block, Item.Properties, BlockItem> itemFactory,
            String recipe,
            Map<String, Item> recipe_map) {
        var block = blockSupplier.get();
        var item = itemFactory.apply(block, new Item.Properties());
        var definition = new BlockDefinition<>(englishName, CrazyAddons.makeId(id), block, item);
        BLOCKS.add(definition);
        BLOCK_RECIPES.put(definition, Map.entry(recipe, recipe_map));
        return definition;
    }
}