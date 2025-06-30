package net.oktawia.crazyae2addons.entities;

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
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import org.jetbrains.annotations.NotNull;

public class PenrosePortBE extends AENetworkBlockEntity implements IGridTickable {

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

    public PenrosePortBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.PENROSE_PORT_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.PENROSE_PORT.get().asItem())
                );
    }

    public void setController(PenroseControllerBE controller) {
        this.controller = controller;
        if (this.controller != null){
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

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (controller == null || level == null) return TickRateModulation.IDLE;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor == null || neighbor instanceof PenroseFrameBE || neighbor instanceof PenroseCoilBE) continue;

            LazyOptional<IEnergyStorage> cap = neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite());

            cap.ifPresent(target -> {
                int maxOutput = controller.energyStorage.extractEnergy(Integer.MAX_VALUE, true);
                int maxAccept = target.receiveEnergy(maxOutput, true);
                if (maxAccept > 0) {
                    int extracted = controller.energyStorage.extractEnergy(maxAccept, false);
                    target.receiveEnergy(extracted, false);
                }
            });
        }
        return TickRateModulation.IDLE;
    }
}
