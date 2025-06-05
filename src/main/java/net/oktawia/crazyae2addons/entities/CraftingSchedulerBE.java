package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.ConfigInventory;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.CraftingSchedulerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class CraftingSchedulerBE extends AENetworkBlockEntity implements MenuProvider, ICraftingRequester, IGridTickable {

    public ConfigInventory inv = ConfigInventory.configTypes(what -> true, 1, () -> {});
    public Integer amount = 0;
    public HashSet<ICraftingLink> craftingJobs = new HashSet<>();
    public List<Future<ICraftingPlan>> toCraftPlans = new ArrayList<>();

    public CraftingSchedulerBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.CRAFTING_SHEDULER_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(1.0F)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.CRAFTING_SCHEDULER_BLOCK.get().asItem())
                );
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CraftingSchedulerMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crafting Sheduler");
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.CRAFTING_SCHEDULER_MENU.get(), player, locator);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(this.craftingJobs);
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        var grid = this.getMainNode().getGrid();
        if (grid == null) return 0;
        var energy = grid.getEnergyService();
        var storage = grid.getStorageService().getInventory();
        return StorageHelper.poweredInsert(energy, storage, what, amount, IActionSource.ofMachine(this), mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingJobs.remove(link);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("config")) {
            this.inv.readFromChildTag(data, "config");
        }
        if (data.contains("amount")) {
            this.amount = data.getInt("amount");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.inv.writeToChildTag(data, "config");
        data.putInt("amount", this.amount);
    }

    public void doWork() {
        var grid = this.getMainNode().getGrid();
        if (grid == null) return;
        var aviableCpus = grid.getCraftingService().getCpus().stream().filter(cpu -> !cpu.isBusy()).count();
        if (grid.getCraftingService().isCraftable(inv.getKey(0)) && aviableCpus > this.toCraftPlans.size()){
            this.toCraftPlans.add(getGridNode().getGrid().getCraftingService().beginCraftingCalculation(
                    getLevel(),
                    () -> new MachineSource(this),
                    inv.getKey(0),
                    amount,
                    CalculationStrategy.REPORT_MISSING_ITEMS
            ));
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        Iterator<Future<ICraftingPlan>> iterator = toCraftPlans.iterator();
        while (iterator.hasNext()) {
            Future<ICraftingPlan> craftingPlan = iterator.next();
            if (craftingPlan.isDone()) {
                try {
                    if (getGridNode() == null) return TickRateModulation.IDLE;
                    var result = getGridNode().getGrid().getCraftingService().submitJob(
                            craftingPlan.get(), this, null, true, IActionSource.ofMachine(this));
                    if (result.successful() && result.link() != null) {
                        this.craftingJobs.add(result.link());
                        iterator.remove();
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return TickRateModulation.IDLE;
    }
}
