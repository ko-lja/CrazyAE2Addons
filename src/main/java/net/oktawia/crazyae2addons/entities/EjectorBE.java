package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.CraftingCancelerMenu;
import net.oktawia.crazyae2addons.menus.EjectorMenu;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class EjectorBE extends AENetworkInvBlockEntity implements MenuProvider, IUpgradeableObject {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    public AppEngInternalInventory sendInv = new AppEngInternalInventory(this, 99);
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(CrazyBlockRegistrar.EJECTOR_BLOCK.get(), 2, this::saveChanges);

    public EjectorBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.EJECTOR_BE.get(), pos, blockState);
        this.getMainNode().setIdlePowerUsage(2.0F).setFlags(GridFlags.REQUIRE_CHANNEL)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.EJECTOR_BLOCK.get().asItem())
                );
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if(data.contains("inv")){
            this.inv.readFromNBT(data, "inv");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data){
        super.saveAdditional(data);
        this.inv.writeToNBT(data, "inv");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EjectorMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Ejector");
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.EJECTOR_MENU.get(), player, locator);
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.setChanged();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    public static boolean areAllInputsAvailable(IPatternDetails.IInput[] inputs, KeyCounter counter) {
        for (IPatternDetails.IInput input : inputs) {
            boolean satisfied = false;

            GenericStack[] options = input.getPossibleInputs();

            for (GenericStack option : options) {
                long available = counter.get(option.what());

                if (available >= option.amount()) {
                    satisfied = true;
                    break;
                }
            }

            if (!satisfied) {
                return false;
            }
        }

        return true;
    }

    public void doWork() {
        if (getGridNode() == null || getGridNode().getGrid() == null || !getMainNode().isActive()) return;
        ItemStack patternStack = this.inv.getStackInSlot(0);
        if (!(patternStack.getItem() instanceof EncodedPatternItem patternItem)) return;

        var grid = getMainNode().getGrid();
        var storage = grid.getStorageService().getInventory();

        var pattern = patternItem.decode(patternStack, this.getLevel(), false);
        if (pattern == null) return;
        if (!pattern.supportsPushInputsToExternalInventory()) return;

        BlockState state = this.getBlockState();
        Direction front = state.getValue(BlockStateProperties.FACING);
        BlockPos frontPos = this.getBlockPos().relative(front);
        BlockEntity targetBE = this.level.getBlockEntity(frontPos);
        if (targetBE == null) return;

        var inputs = pattern.getInputs();
        var available = storage.getAvailableStacks();
        if (!areAllInputsAvailable(inputs, storage.getAvailableStacks())) return;

        KeyCounter[] inputCounters = new KeyCounter[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputCounters[i] = new KeyCounter();
            for (GenericStack option : inputs[i].getPossibleInputs()) {
                long availableAmount = available.get(option.what());
                if (availableAmount >= option.amount()) {
                    var amt = option.what().getAmountPerUnit() * option.amount();
                    inputCounters[i].add(option.what(), amt);
                    available.remove(option.what(), amt);
                    break;
                }
            }
        }

        var target = PatternProviderTarget.get(getLevel(), frontPos, targetBE, front.getOpposite(), IActionSource.ofMachine(this));
        if (target == null) return;
        pattern.pushInputsToExternalInventory(inputCounters, (x, y) -> {
            var inserted = target.insert(x, y, Actionable.MODULATE);
            storage.extract(x, inserted, Actionable.MODULATE, IActionSource.ofMachine(this));
        });

    }
}
