package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.DataSetterMenu;
import net.oktawia.crazyae2addons.menus.DataTrackerMenu;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

public class DataSetterBE extends NotifyableBlockEntity implements MenuProvider {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);
    public String variableToSet = "";
    public Integer valueToSet = 0;
    public String hexId = randomHexId();

    public DataSetterBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.DATA_SETTER_BE.get(), pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.DATA_SETTER_BLOCK.get().asItem())
                );
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int val = rand.nextInt(16); // 0-15
            sb.append(Integer.toHexString(val).toUpperCase());
        }
        return sb.toString();
    }

    @Override
    public void doNotify(String name, Integer value, Integer depth) {}

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {}

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putString("variable", this.variableToSet);
        data.putInt("value", this.valueToSet);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("variable")) {
            this.variableToSet = data.getString("variable");
        }
        if (data.contains("value")) {
            this.valueToSet = data.getInt("value");
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DataSetterMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.DATA_SETTER_MENU.get(), player, locator);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void onRedstoneActivate(){
        if (getMainNode().getGrid() == null) return;
        var controllers = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList();
        if (controllers.isEmpty()) return;
        MEDataControllerBE database = controllers.get(0);
        database.addVariable(this.hexId, this.variableToSet, this.valueToSet, 0);
    }
}
