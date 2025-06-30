package net.oktawia.crazyae2addons.entities;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.oktawia.crazyae2addons.blocks.EnergyStorageController;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import org.jetbrains.annotations.NotNull;

public class EnergyStorageFrameBE extends AENetworkBlockEntity {

    private EnergyStorageControllerBE controller;

    public EnergyStorageFrameBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.ENERGY_STORAGE_FRAME_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_FRAME_BLOCK.get().asItem())
                );
    }

    public void setController(EnergyStorageControllerBE controller) {
        this.controller = controller;
        if (getMainNode().getNode() != null){
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
}
