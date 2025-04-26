package net.oktawia.crazyae2addons.defs.regs;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.compat.GregTech.*;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CrazyBlockEntityRegistrar {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CrazyAddons.MODID);

    private static final List<Runnable> BLOCK_ENTITY_SETUP = new ArrayList<>();

    private static <T extends AEBaseBlockEntity> RegistryObject<BlockEntityType<T>> reg(
            String id,
            RegistryObject<? extends AEBaseEntityBlock<?>> block,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Class<T> blockEntityClass
    ) {
        return BLOCK_ENTITIES.register(id, () -> {
            var blk = block.get();
            var type = BlockEntityType.Builder.of(factory, blk).build(null);

            BLOCK_ENTITY_SETUP.add(() -> blk.setBlockEntity(
                    (Class) blockEntityClass, (BlockEntityType) type, null, null
            ));

            return type;
        });
    }

    public static final RegistryObject<BlockEntityType<CraftingCancelerBE>> CRAFTING_CANCELER_BE =
            reg("crafting_canceler_be", CrazyBlockRegistrar.CRAFTING_CANCELER_BLOCK, CraftingCancelerBE::new, CraftingCancelerBE.class);

    public static final RegistryObject<BlockEntityType<MEDataControllerBE>> ME_DATA_CONTROLLER_BE =
            reg("me_data_controller_be", CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK, MEDataControllerBE::new, MEDataControllerBE.class);

    public static final RegistryObject<BlockEntityType<DataProcessorBE>> DATA_PROCESSOR_BE =
            reg("data_processor_be", CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK, DataProcessorBE::new, DataProcessorBE.class);

    public static final RegistryObject<BlockEntityType<DataTrackerBE>> DATA_TRACKER_BE =
            reg("data_tracker_be", CrazyBlockRegistrar.DATA_TRACKER_BLOCK, DataTrackerBE::new, DataTrackerBE.class);

    public static final RegistryObject<BlockEntityType<IsolatedDataProcessorBE>> ISOLATED_DATA_PROCESSOR_BE =
            reg("isolated_data_processor_be", CrazyBlockRegistrar.ISOLATED_DATA_PROCESSOR_BLOCK, IsolatedDataProcessorBE::new, IsolatedDataProcessorBE.class);

    public static final RegistryObject<BlockEntityType<ImpulsedPatternProviderBE>> IMPULSED_PATTERN_PROVIDER_BE =
            reg("impulsed_pp_be", CrazyBlockRegistrar.IMPULSED_PATTERN_PROVIDER_BLOCK, ImpulsedPatternProviderBE::new, ImpulsedPatternProviderBE.class);

    public static final RegistryObject<BlockEntityType<SignallingInterfaceBE>> SIGNALLING_INTERFACE_BE =
            reg("signalling_interface_be", CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK, SignallingInterfaceBE::new, SignallingInterfaceBE.class);

    public static final RegistryObject<BlockEntityType<CircuitedPatternProviderBE>> CIRCUITED_PATTERN_PROVIDER_BE =
            IsModLoaded.isGTCEuLoaded()
                    ? reg("circuited_pp_be", CrazyBlockRegistrar.CIRCUITED_PATTERN_PROVIDER_BLOCK, CircuitedPatternProviderBE::new, CircuitedPatternProviderBE.class)
                    : null;

    public static final RegistryObject<BlockEntityType<? extends AmpereMeterBE>> AMPERE_METER_BE =
            BLOCK_ENTITIES.register("ampere_meter_be", () -> {
                var blk = CrazyBlockRegistrar.AMPERE_METER_BLOCK.get();
                if (IsModLoaded.isGTCEuLoaded()) {
                    var type = BlockEntityType.Builder.of(GTAmpereMeterBE::new, blk).build(null);
                    BLOCK_ENTITY_SETUP.add(() -> ((AEBaseEntityBlock) blk).setBlockEntity(GTAmpereMeterBE.class, type, null, null));
                    return type;
                } else {
                    var type = BlockEntityType.Builder.of(AmpereMeterBE::new, blk).build(null);
                    BLOCK_ENTITY_SETUP.add(() -> blk.setBlockEntity(AmpereMeterBE.class, type, null, null));
                    return type;
                }
            });

    public static void setupBlockEntityTypes() {
        for (var runnable : BLOCK_ENTITY_SETUP) {
            runnable.run();
        }
    }

    public static List<? extends BlockEntityType<?>> getEntities() {
        return BLOCK_ENTITIES.getEntries().stream().map(RegistryObject::get).toList();
    }
}