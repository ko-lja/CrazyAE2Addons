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
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.entities.*;

public class BlockEntities {
    private static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();
    public static final BlockEntityType<CraftingCancelerBE> CRAFTING_CANCELER_BE = create(
            "crafting_canceler_be",
            CraftingCancelerBE.class,
            CraftingCancelerBE::new,
            Blocks.CRAFTING_CANCELER_BLOCK);
    public static final BlockEntityType<PatternModifierBE> PATTERN_MODIFIER_BE = create(
            "pattern_modifier_be",
            PatternModifierBE.class,
            PatternModifierBE::new,
            Blocks.PATTERN_MODIFIER_BLOCK);

    public static final BlockEntityType<AutoEnchanterBE> AUTO_ENCHANTER_BE = create(
            "auto_enchanter_be",
            AutoEnchanterBE.class,
            AutoEnchanterBE::new,
            Blocks.AUTO_ENCHANTER_BLOCK);

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