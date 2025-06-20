package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import org.jetbrains.annotations.NotNull;

public class PenroseFrameBE extends AENetworkBlockEntity {

    private PenroseControllerBE controller;

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> new IEnergyStorage() {
        @Override public int getEnergyStored() {
            return controller != null ? controller.energyStorage.getEnergyStored() : 0;
        }
        @Override public int getMaxEnergyStored() {
            return controller != null ? controller.energyStorage.getMaxEnergyStored() : 0;
        }
        @Override public boolean canExtract() {
            return controller != null;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            return controller != null
                    ? controller.energyStorage.extractEnergy(maxExtract, simulate)
                    : 0;
        }
        @Override public boolean canReceive() {
            return false;
        }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }
    });

    public PenroseFrameBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.PENROSE_FRAME_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.PENROSE_FRAME.get().asItem())
                );
    }

    public void setController(PenroseControllerBE controller) {
        this.controller = controller;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap);
    }
}
