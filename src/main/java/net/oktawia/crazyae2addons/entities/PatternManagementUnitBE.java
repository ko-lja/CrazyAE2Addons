package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;

public class PatternManagementUnitBE extends AENetworkBlockEntity {

    public PatternManagementUnitControllerBE controller;

    public PatternManagementUnitBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.PATTERN_MANAGEMENT_UNIT_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_BLOCK.get().asItem())
                );
    }
}
