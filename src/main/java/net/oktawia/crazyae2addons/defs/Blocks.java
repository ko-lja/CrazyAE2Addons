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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.items.*;
import net.oktawia.crazyae2addons.CrazyAddons;

public class Blocks {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, CrazyAddons.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, CrazyAddons.MODID);
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    private static final Map<BlockDefinition<?>, Map.Entry<String, Map<String, Item>>> BLOCK_RECIPES = new HashMap<>();
    public static final BlockDefinition<CraftingCancelerBlock> CRAFTING_CANCELER_BLOCK = block(
            "Crafting Canceler",
            "crafting_canceler_block",
            CraftingCancelerBlock::new,
            CraftingCancelerBlockItem::new,
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
            MEDataControllerBlock::new,
            MEDataControllerBlockItem::new,
            "SLS/LCL/SLS",
            () -> Map.of(
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
            () -> Map.of(
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
            () -> Map.of(
                    "T", Items.REDSTONE_TORCH,
                    "S", AEBlocks.SKY_STONE_BLOCK.asItem(),
                    "L", net.oktawia.crazyae2addons.defs.Items.LOGIC_CARD.asItem()
            )
    );
    public static final BlockDefinition<CircuitedPatternProviderBlock> CIRCUITED_PATTERN_PROVIDER_BLOCK =
            IsModLoaded.isGTCEuLoaded() ?
                block(
                    "Circuited Pattern Provider",
                    "circuited_pp",
                    CircuitedPatternProviderBlock::new,
                    CircuitedPatternProviderBlockItem::new,
                    "PCC",
                    () -> Map.of(
                        "P", AEBlocks.PATTERN_PROVIDER.asItem(),
                        "C", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
                    )
            ) : null;
    public static final BlockDefinition<AmpereMeterBlock> AMPERE_METER_BLOCK = block(
            "Ampere Meter",
            "ampere_meter",
            AmpereMeterBlock::new,
            AmpereMeterBlockItem::new,
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
            IsolatedDataProcessorBlock::new,
            IsolatedDataProcessorBlockItem::new,
            "WWW/WCW/WWW",
            () -> Map.of(
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
            () -> Map.of(
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
            Supplier<T> blockSupplier,
            BiFunction<Block, Item.Properties, BlockItem> itemFactory,
            String recipe,
            Supplier<Map<String, Item>> recipeMapSupplier
    ) {
        T blockInstance = blockSupplier.get();
        BlockItem blockItemInstance = itemFactory.apply(blockInstance, new Item.Properties());
        BLOCK_REGISTER.register(id, () -> blockInstance);
        BLOCK_ITEM_REGISTER.register(id, () -> blockItemInstance);
        var definition = new BlockDefinition<>(englishName, CrazyAddons.makeId(id), blockInstance, blockItemInstance);
        BLOCKS.add(definition);
        BLOCK_RECIPES.put(definition, Map.entry(recipe, recipeMapSupplier.get()));
        return definition;
    }
}