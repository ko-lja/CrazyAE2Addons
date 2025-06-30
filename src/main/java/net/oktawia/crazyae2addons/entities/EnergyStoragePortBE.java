package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.oktawia.crazyae2addons.blocks.EnergyStorageController;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import org.jetbrains.annotations.NotNull;

public class EnergyStoragePortBE extends AENetworkBlockEntity {

    private EnergyStorageControllerBE controller;

    public EnergyStoragePortBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.ENERGY_STORAGE_PORT_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_PORT_BLOCK.get().asItem())
                );
    }

    private final LazyOptional<IEnergyStorage> energyStorage = LazyOptional.of(() -> new IEnergyStorage() {
        @Override public int getEnergyStored() {
            if (controller == null) return 0;
            return (int) controller.getAECurrentPower();
        }
        @Override public int getMaxEnergyStored() {
            if (controller == null) return 0;
            return (int) controller.getAEMaxPower();
        }
        @Override public boolean canExtract() {
            return true;
        }
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (controller == null) return 0;

            return (int) (controller.extractAEPower(maxExtract, simulate ? Actionable.SIMULATE : Actionable.MODULATE, PowerMultiplier.CONFIG));
        }
        @Override public boolean canReceive() {
            return true;
        }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            if (controller == null) return 0;
            return (int) (maxReceive - controller.injectAEPower(maxReceive, simulate ? Actionable.SIMULATE : Actionable.MODULATE));
        }
    });

    public void setController(EnergyStorageControllerBE controller) {
        this.controller = controller;
        if (getMainNode().getNode() != null) {
            if (this.controller != null && this.controller.getMainNode().getNode() != null){
                if (getMainNode().getNode().getConnections().stream()
                        .noneMatch(x -> (x.a() == this.controller.getMainNode().getNode() || x.b() == this.controller.getMainNode().getNode()))){
                    GridHelper.createConnection(getMainNode().getNode(), this.controller.getMainNode().getNode());
                }
            } else {
                getMainNode().getNode().getConnections().stream()
                        .filter(x -> (!x.isInWorld()))
                        .forEach(IGridConnection::destroy);
            }
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorage.cast();
        }
        return super.getCapability(cap);
    }
}
