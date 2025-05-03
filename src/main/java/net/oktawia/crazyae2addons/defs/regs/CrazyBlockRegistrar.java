package net.oktawia.crazyae2addons.defs.regs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.items.*;

import java.util.List;

public class CrazyBlockRegistrar {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrazyAddons.MODID);

    public static List<Block> getBlocks() {
        return BLOCKS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .toList();
    }

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CrazyAddons.MODID);

    public static final RegistryObject<CraftingCancelerBlock> CRAFTING_CANCELER_BLOCK =
            BLOCKS.register("crafting_canceler", CraftingCancelerBlock::new);

    public static final RegistryObject<BlockItem> CRAFTING_CANCELER_BLOCK_ITEM =
            BLOCK_ITEMS.register("crafting_canceler",
                    () -> new CraftingCancelerBlockItem(CRAFTING_CANCELER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<MEDataControllerBlock> ME_DATA_CONTROLLER_BLOCK =
            BLOCKS.register("me_data_controller", MEDataControllerBlock::new);

    public static final RegistryObject<BlockItem> ME_DATA_CONTROLLER_BLOCK_ITEM =
            BLOCK_ITEMS.register("me_data_controller",
                    () -> new MEDataControllerBlockItem(ME_DATA_CONTROLLER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<DataProcessorBlock> DATA_PROCESSOR_BLOCK =
            BLOCKS.register("data_processor", DataProcessorBlock::new);

    public static final RegistryObject<BlockItem> DATA_PROCESSOR_BLOCK_ITEM =
            BLOCK_ITEMS.register("data_processor",
                    () -> new DataProcessorBlockItem(DATA_PROCESSOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<DataTrackerBlock> DATA_TRACKER_BLOCK =
            BLOCKS.register("data_tracker", DataTrackerBlock::new);

    public static final RegistryObject<BlockItem> DATA_TRACKER_BLOCK_ITEM =
            BLOCK_ITEMS.register("data_tracker",
                    () -> new DataTrackerBlockItem(DATA_TRACKER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<CircuitedPatternProviderBlock> CIRCUITED_PATTERN_PROVIDER_BLOCK =
            IsModLoaded.isGTCEuLoaded()
                    ? BLOCKS.register("circuited_pattern_provider", CircuitedPatternProviderBlock::new)
                    : null;

    public static final RegistryObject<BlockItem> CIRCUITED_PATTERN_PROVIDER_BLOCK_ITEM =
            IsModLoaded.isGTCEuLoaded()
                    ? BLOCK_ITEMS.register("circuited_pattern_provider",
                    () -> new CircuitedPatternProviderBlockItem(
                            CIRCUITED_PATTERN_PROVIDER_BLOCK.get(), new Item.Properties()))
                    : null;

    public static final RegistryObject<AmpereMeterBlock> AMPERE_METER_BLOCK =
            BLOCKS.register("ampere_meter", AmpereMeterBlock::new);

    public static final RegistryObject<BlockItem> AMPERE_METER_BLOCK_ITEM =
            BLOCK_ITEMS.register("ampere_meter",
                    () -> new AmpereMeterBlockItem(AMPERE_METER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<IsolatedDataProcessorBlock> ISOLATED_DATA_PROCESSOR_BLOCK =
            BLOCKS.register("isolated_data_processor", IsolatedDataProcessorBlock::new);

    public static final RegistryObject<BlockItem> ISOLATED_DATA_PROCESSOR_BLOCK_ITEM =
            BLOCK_ITEMS.register("isolated_data_processor",
                    () -> new IsolatedDataProcessorBlockItem(ISOLATED_DATA_PROCESSOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<ImpulsedPatternProviderBlock> IMPULSED_PATTERN_PROVIDER_BLOCK =
            BLOCKS.register("impulsed_pattern_provider", ImpulsedPatternProviderBlock::new);

    public static final RegistryObject<BlockItem> IMPULSED_PATTERN_PROVIDER_BLOCK_ITEM =
            BLOCK_ITEMS.register("impulsed_pattern_provider",
                    () -> new ImpulsedPatternProviderBlockItem(IMPULSED_PATTERN_PROVIDER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<SignallingInterfaceBlock> SIGNALLING_INTERFACE_BLOCK =
            BLOCKS.register("signalling_interface", SignallingInterfaceBlock::new);

    public static final RegistryObject<BlockItem> SIGNALLING_INTERFACE_BLOCK_ITEM =
            BLOCK_ITEMS.register("signalling_interface",
                    () -> new SignallingInterfaceBlockItem(SIGNALLING_INTERFACE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<MobFarmWall> MOB_FARM_WALL_BLOCK =
            BLOCKS.register("mob_farm_wall", MobFarmWall::new);

    public static final RegistryObject<BlockItem> MOB_FARM_WALL_BLOCK_ITEM =
            BLOCK_ITEMS.register("mob_farm_wall",
                    () -> new MobFarmWallItem(MOB_FARM_WALL_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<MobFarmCollector> MOB_FARM_COLLECTOR_BLOCK =
            BLOCKS.register("mob_farm_collector", MobFarmCollector::new);

    public static final RegistryObject<BlockItem> MOB_FARM_COLLECTOR_BLOCK_ITEM =
            BLOCK_ITEMS.register("mob_farm_collector",
                    () -> new MobFarmCollectorItem(MOB_FARM_COLLECTOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<MobFarmInput> MOB_FARM_INPUT_BLOCK =
            BLOCKS.register("mob_farm_input", MobFarmInput::new);

    public static final RegistryObject<BlockItem> MOB_FARM_INPUT_BLOCK_ITEM =
            BLOCK_ITEMS.register("mob_farm_input",
                    () -> new MobFarmInputItem(MOB_FARM_INPUT_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<MobFarmDamageModule> MOB_FARM_DAMAGE_MODULE_BLOCK =
            BLOCKS.register("mob_farm_damage_module", MobFarmDamageModule::new);

    public static final RegistryObject<BlockItem> MOB_FARM_DAMAGE_MODULE_BLOCK_ITEM =
            BLOCK_ITEMS.register("mob_farm_damage_module",
                    () -> new MobFarmDamageModuleItem(MOB_FARM_DAMAGE_MODULE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<SpawnerControllerWall> SPAWNER_CONTROLLER_WALL_BLOCK =
            BLOCKS.register("spawner_controller_wall", SpawnerControllerWall::new);

    public static final RegistryObject<BlockItem> SPAWNER_CONTROLLER_WALL_BLOCK_ITEM =
            BLOCK_ITEMS.register("spawner_controller_wall",
                    () -> new MobFarmDamageModuleItem(SPAWNER_CONTROLLER_WALL_BLOCK.get(), new Item.Properties()));

    private CrazyBlockRegistrar() {}
}