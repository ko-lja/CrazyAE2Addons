package net.oktawia.crazyae2addons.defs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.compat.GregTech.GTAmpereMeterBE;
import net.oktawia.crazyae2addons.entities.*;

public class BlockEntities {
    private static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();

    public static final BlockEntityType<CraftingCancelerBE> CRAFTING_CANCELER_BE = create(
            "crafting_canceler_be",
            CraftingCancelerBE.class,
            CraftingCancelerBE::new,
            Blocks.CRAFTING_CANCELER_BLOCK);

    public static final BlockEntityType<MEDataControllerBE> ME_DATA_CONTROLLER_BE = create(
            "me_data_controller_be",
            MEDataControllerBE.class,
            MEDataControllerBE::new,
            Blocks.ME_DATA_CONTROLLER_BLOCK);

    public static final BlockEntityType<DataProcessorBE> DATA_PROCESSOR_BE = create(
            "data_processor_be",
            DataProcessorBE.class,
            DataProcessorBE::new,
            Blocks.DATA_PROCESSOR_BLOCK);

    public static final BlockEntityType<DataTrackerBE> DATA_TRACKER_BE = create(
            "data_tracker_be",
            DataTrackerBE.class,
            DataTrackerBE::new,
            Blocks.DATA_TRACKER_BLOCK);

    public static final BlockEntityType<IsolatedDataProcessorBE> ISOLATED_DATA_PROCESSOR_BE = create(
            "isolated_data_processor_be",
            IsolatedDataProcessorBE.class,
            IsolatedDataProcessorBE::new,
            Blocks.ISOLATED_DATA_PROCESSOR_BLOCK);

    public static final BlockEntityType<ImpulsedPatternProviderBE> IMPULSED_PATTERN_PROVIDER_BE = create(
            "impulsed_pp_be",
            ImpulsedPatternProviderBE.class,
            ImpulsedPatternProviderBE::new,
            Blocks.IMPULSED_PATTERN_PROVIDER_BLOCK);

    public static final BlockEntityType<SignallingInterfaceBE> SIGNALLING_INTERFACE_BE = create(
            "signalling_interface_be",
            SignallingInterfaceBE.class,
            SignallingInterfaceBE::new,
            Blocks.SIGNALLING_INTERFACE_BLOCK);

    public static final BlockEntityType<? extends AEBaseBlockEntity> CIRCUITED_PATTERN_PROVIDER_BE =
            IsModLoaded.isGTCEuLoaded()
                    ? create(
                            "circuited_pp_be",
                            CircuitedPatternProviderBE.class,
                            CircuitedPatternProviderBE::new,
                            Blocks.CIRCUITED_PATTERN_PROVIDER_BLOCK
                    ) : null;

    public static final BlockEntityType<? extends AmpereMeterBE> AMPERE_METER_BE =
            IsModLoaded.isGTCEuLoaded()
                    ? create(
                            "ampere_meter_be",
                            GTAmpereMeterBE.class,
                            GTAmpereMeterBE::new,
                            Blocks.AMPERE_METER_BLOCK
                    ) : create(
                    "ampere_meter_be",
                    AmpereMeterBE.class,
                    AmpereMeterBE::new,
                    Blocks.AMPERE_METER_BLOCK
            );

    public static Map<ResourceLocation, BlockEntityType<?>> getBlockEntityTypes() {
        return Collections.unmodifiableMap(BLOCK_ENTITY_TYPES);
    }

    @SafeVarargs
    public static <T extends AEBaseBlockEntity> BlockEntityType<T> create(
            String id,
            Class<T> entityClass,
            BlockEntityFactory<T> factory,
            BlockDefinition<? extends AEBaseEntityBlock<?>>... blockDefinitions) {
        if (blockDefinitions.length == 0) {
            throw new IllegalArgumentException();
        }

        var blocks = Arrays.stream(blockDefinitions).map(BlockDefinition::block).toArray(AEBaseEntityBlock[]::new);

        var typeHolder = new AtomicReference<BlockEntityType<T>>();
        var type = BlockEntityType.Builder.of(
                        (blockPos, blockState) -> factory.create(typeHolder.get(), blockPos, blockState), blocks)
                .build(null);
        typeHolder.set(type);
        BLOCK_ENTITY_TYPES.put(CrazyAddons.makeId(id), type);

        AEBaseBlockEntity.registerBlockEntityItem(type, blockDefinitions[0].asItem());

        for (var block : blocks) {
            var baseBlock = (AEBaseEntityBlock<T>) block;
            baseBlock.setBlockEntity(entityClass, type, null, null);
        }

        return type;
    }

    public interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}