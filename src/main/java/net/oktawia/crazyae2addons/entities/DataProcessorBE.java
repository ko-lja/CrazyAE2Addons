package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;

public class DataProcessorBE extends AEBaseBlockEntity {

    public DataProcessorBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.DATA_PROCESSOR_BE.get(), pos, blockState);
    }

}
