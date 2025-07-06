package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;

public class PatternManagementUnitWallBE extends AENetworkBlockEntity {

    public PatternManagementUnitControllerBE controller;

    public PatternManagementUnitWallBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.PATTERN_MANAGEMENT_UNIT_WALL_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_WALL_BLOCK.get().asItem())
                );
    }
}
