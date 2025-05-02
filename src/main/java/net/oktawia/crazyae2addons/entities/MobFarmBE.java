package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.clusters.MobFarmPart;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.MobFarmMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobFarmWallBE extends MobFarmPart implements IGridTickable, MenuProvider, IUpgradeableObject {

    public MobFarmWallBE(BlockPos pos, BlockState state) {
        super(CrazyBlockEntityRegistrar.MOB_FARM_WALL_BE.get(), pos, state);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        if (this.cluster != null) {
            return this.cluster.getUpgrades();
        }
        return UpgradeInventories.empty();
    }

    public InternalInventory getInventory() {
        if (this.cluster != null) {
            return this.cluster.getInventory();
        }
        return InternalInventory.empty();
    }

    public void openMenu(Player player, MenuLocator locator) {
        if (this.getCluster() != null){
            MenuOpener.open(CrazyMenuRegistrar.MOB_FARM_MENU.get(), player, locator);
        }
    }

    @Override
    public TickingRequest getTickingRequest(appeng.api.networking.IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(appeng.api.networking.IGridNode node, int ticksSinceLastCall) {
        return TickRateModulation.IDLE;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Mob Farm");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
        return new MobFarmMenu(id, inventory, this);
    }
}