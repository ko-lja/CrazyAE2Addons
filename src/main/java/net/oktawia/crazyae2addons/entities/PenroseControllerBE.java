package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.PenroseControllerMenu;
import net.oktawia.crazyae2addons.misc.PenroseValidator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PenroseControllerBE extends AENetworkInvBlockEntity implements MenuProvider, IUpgradeableObject, IGridTickable {

    private int ticks = 0;
    public EnergyStorage energyStorage = new EnergyStorage(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0);

    public PenroseValidator validator;
    public AppEngInternalInventory diskInv = new AppEngInternalInventory(this, 1, 1, new IAEItemFilter() {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (stack.getItem() == AEItems.ITEM_CELL_4K.asItem()) {
                var cellInv = StorageCells.getCellInventory(stack, null);
                if (cellInv == null) return false;
                var stacks = cellInv.getAvailableStacks();
                if (stacks.isEmpty()) return true;
                return stacks.size() == 1 && Objects.equals(stacks.getFirstKey(), AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
            }
            return false;
        }
    });

    public AppEngInternalInventory inputInv = new AppEngInternalInventory(this, 1, 16384, new IAEItemFilter() {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return stack.getItem() == CrazyItemRegistrar.SUPER_SINGULARITY.get();
        }
    });

    public ConfigInventory config = ConfigInventory.configTypes(
            key -> key instanceof AEItemKey itemkey && itemkey.toStack().getItem() != CrazyItemRegistrar.SUPER_SINGULARITY.get(), 1, () -> {});
    private boolean formed;

    public PenroseControllerBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.PENROSE_CONTROLLER_BE.get(), pos, blockState);
        validator = new PenroseValidator();
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.PENROSE_CONTROLLER.get().asItem())
                );
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        drops.add(this.inputInv.getStackInSlot(0));
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.config.readFromChildTag(data, "config");
        this.inputInv.readFromNBT(data, "inputinv");
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.config.writeToChildTag(data, "config");
        this.inputInv.writeToNBT(data, "inputinv");
    }


    public static long energyGenerated(int count) {
        final int MAX_COUNT   = 32768;
        final long MAX_ENERGY = (1L << 30) / 64;

        if (count <= 0) {
            return 0L;
        }
        if (count >= MAX_COUNT) {
            return MAX_ENERGY;
        }
        return (long) count * MAX_ENERGY / MAX_COUNT;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new PenroseControllerMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Penrose Controller");
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.PENROSE_CONTROLLER_MENU.get(), player, locator);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        ticks ++;
        if (ticks >= 20){
            this.formed = validator.matchesStructure(getLevel(), getBlockPos(), getBlockState(), this);
            ticks = 0;
        }
        if (this.formed){
            var grid = getMainNode().getGrid();
            if (grid == null) return TickRateModulation.IDLE;
            var inv = grid.getStorageService().getInventory();
            var disk = StorageCells.getCellInventory(diskInv.getStackInSlot(0), null);
            if (disk == null) return TickRateModulation.IDLE;
            if (config.getStack(0) == null) return TickRateModulation.IDLE;
            var extracted = inv.extract(this.config.getKey(0), 1, Actionable.MODULATE, IActionSource.ofMachine(this));
            int generated = Math.toIntExact(energyGenerated((int) disk.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()))) * extracted);
            if (AEItems.MATTER_BALL.isSameAs(((AEItemKey) config.getStack(0).what()).toStack())){
                generated *= 8;
            } else if (AEItems.SINGULARITY.isSameAs(((AEItemKey) config.getStack(0).what()).toStack())){
                generated *= 64;
            }
            this.energyStorage = new EnergyStorage(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, (int) Math.min(((long)Integer.MAX_VALUE), ((long)this.energyStorage.getEnergyStored()) + generated));
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return diskInv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
    }
}
