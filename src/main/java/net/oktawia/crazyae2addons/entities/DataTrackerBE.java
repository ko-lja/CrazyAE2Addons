package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.DataTrackerMenu;
import org.jetbrains.annotations.Nullable;

public class DataTrackerBE extends NotifyableBlockEntity implements MenuProvider, IGridTickable {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);
    public String trackedVariable = "";
    public boolean active = false;
    public boolean reRegister = false;

    public DataTrackerBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void doNotify(String name, Integer value, Integer depth) {
        if (this.getLevel() != null){
            this.active = value > 0;
            this.getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        return super.getSubInventory(id);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putString("variable", this.trackedVariable);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("variable")) {
            this.trackedVariable = data.getString("variable");
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DataTrackerMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.DATA_TRACKER_MENU, player, locator);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void setTracked(String value) {
        this.trackedVariable = value;
        this.active = false;
        if (this.getGridNode() == null || this.getGridNode().getGrid() == null || this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()){
            this.reRegister = true;
            return;
        }
        MEDataControllerBE controller = this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        if (controller.getMaxVariables() <= 0){
            this.reRegister = true;
            return;
        }
        this.active = false;
        this.getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        controller.unRegisterNotification(this);
        if (!value.isEmpty()){
            controller.registerNotification(value.replace("&", ""), this);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.getGridNode() == null || this.getGridNode().getGrid() == null || this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()){
            this.reRegister = true;
            this.active = false;
            this.getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        } else {
            MEDataControllerBE controller = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
            if (controller.getMaxVariables() <= 0){
                this.reRegister = true;
                this.active = false;
                this.getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            } else {
                if (this.reRegister){
                    this.reRegister = false;
                    setTracked(this.trackedVariable);
                }
            }
        }
        return TickRateModulation.IDLE;
    }
}
