package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.BlockEntities;
import net.oktawia.crazyae2addons.menus.CraftingCancelerMenu;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;
import java.util.List;

public class CraftingCancelerBE extends AENetworkBlockEntity implements MenuProvider, IGridTickable, IUpgradeableObject {
    private boolean enabled;
    private int duration;
    private List<ICraftingCPU> craftingCpus;
    private Instant intervalStart;
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    public CraftingCancelerBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.duration = 0;
        this.enabled = false;
        this.getMainNode().setIdlePowerUsage((double)4.0F).addService(IGridTickable.class, this).setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(1, 5, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode iGridNode, int ticksSinceLastCall) {
        return TickRateModulation.SAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CraftingCancelerMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crafting Canceler");
    }

    public void setEnabled(boolean state){
        enabled = state;
    }

    public void setDuration(int newDuration){
        duration = newDuration;
    }

    public boolean getEnabled(){
        return enabled;
    }

    public int getDuration(){
        return duration;
    }

}
