package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.*;
import appeng.api.storage.AEKeyFilter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.ConfigInventory;
import com.google.common.collect.ImmutableSet;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.oktawia.crazyae2addons.blocks.EjectorBlock;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.EjectorMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Future;

public class EjectorBE extends AENetworkBlockEntity implements MenuProvider, IUpgradeableObject, ICraftingRequester, IGridTickable {

    private final List<GenericStack> leftoversToInsert = new ArrayList<>();

    public ConfigInventory config = ConfigInventory.configStacks(
            AEKeyFilter.none(),
            36,
            null,
            true
    );
    public ConfigInventory storage = ConfigInventory.configStacks(
            AEKeyFilter.none(),
            36,
            null,
            true
    );
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(CrazyBlockRegistrar.EJECTOR_BLOCK.get(), 1, () -> {
        this.doesWait = false;
        this.toCraftPlans.clear();
        this.saveChanges();
    });
    public Boolean doesWait = false;
    public List<Future<ICraftingPlan>> toCraftPlans = new ArrayList<>();
    public List<ICraftingLink> craftingLinks = new ArrayList<>();
    public GenericStack cantCraft = null;
    public EjectorMenu menu = null;

    public EjectorBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.EJECTOR_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(2.0F)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.EJECTOR_BLOCK.get().asItem())
                );
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("config")) {
            this.config.readFromChildTag(data, "config");
        }
        if (data.contains("storage")) {
            this.storage.readFromChildTag(data, "storage");
        }
        if (data.contains("upgrades")) {
            this.upgrades.readFromNBT(data, "upgrades");
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        drops.add(upgrades.getStackInSlot(0));
        for (int i = 0; i < storage.size(); i++) {
            if (storage.getKey(i) instanceof AEItemKey itemKey) {
                drops.add(itemKey.toStack());
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.config.writeToChildTag(data, "config");
        this.storage.writeToChildTag(data, "storage");
        this.upgrades.writeToNBT(data, "upgrades");
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

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.storage.createMenuWrapper();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public void scheduleCrafts(List<GenericStack> toCraft) {
        for (GenericStack stack : toCraft) {
            toCraftPlans.add(getGridNode().getGrid().getCraftingService().beginCraftingCalculation(
                    getLevel(),
                    () -> new MachineSource(this),
                    stack.what(),
                    stack.amount(),
                    CalculationStrategy.REPORT_MISSING_ITEMS
            ));
        }
    }

    public void flushInv(){
        var node = getGridNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;
        var storage = grid.getStorageService();
        if (storage == null) return;

        for (int i = 0; i < this.storage.size(); i++) {
            GenericStack stack = this.storage.getStack(i);
            if (stack == null || stack.amount() <= 0) continue;

            long inserted = storage.getInventory().insert(stack.what(), stack.amount(), Actionable.MODULATE, IActionSource.ofMachine(this));
            if (inserted > 0) {
                long remaining = stack.amount() - inserted;
                if (remaining > 0) {
                    this.storage.setStack(i, new GenericStack(stack.what(), remaining));
                } else {
                    this.storage.setStack(i, null);
                }
            }
        }
    }

    public void doWork() {
        if (getGridNode() == null || getGridNode().getGrid() == null || !getMainNode().isActive() || doesWait) return;

        flushInv();
        List<GenericStack> toCraft = new ArrayList<>();
        var storage = getGridNode().getGrid().getStorageService();

        for (int slot = 0; slot < config.size(); slot++) {
            GenericStack keyStack = config.getStack(slot);
            if (keyStack == null || keyStack.what() == null || keyStack.amount() <= 0) continue;

            AEKey key = keyStack.what();
            long amount = keyStack.amount();
            if (amount > 512 && keyStack.what() instanceof AEItemKey) return;

            long extractedAmount = storage.getInventory().extract(key, amount, Actionable.SIMULATE, IActionSource.ofMachine(this));

            if (extractedAmount < amount) {
                if (!getGridNode().getGrid().getCraftingService().isCraftable(key)) {
                    return;
                }

                GenericStack craftStack = new GenericStack(key, amount - extractedAmount);
                toCraft.add(craftStack);
            }
        }

        for (int slot = 0; slot < config.size(); slot++) {
            GenericStack keyStack = config.getStack(slot);
            if (keyStack == null || keyStack.what() == null || keyStack.amount() <= 0) continue;

            long extracted = storage.getInventory().extract(keyStack.what(), keyStack.amount(), Actionable.MODULATE, IActionSource.ofMachine(this));
            if (extracted > 0) {
                this.storage.setStack(slot, new GenericStack(keyStack.what(), extracted));
            }
        }
        doesWait = true;

        if (!toCraft.isEmpty() && isUpgradedWith(AEItems.CRAFTING_CARD)) {
            this.scheduleCrafts(toCraft);
        }
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(this.craftingLinks);
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        this.craftingLinks.remove(link);

        if (getGridNode() == null || getGridNode().getGrid() == null || !getMainNode().isActive()) {
            return 0;
        }

        long remaining = amount;

        for (int slot = 0; slot < config.size() && remaining > 0; slot++) {
            GenericStack configStack = config.getStack(slot);
            if (configStack == null || !what.equals(configStack.what())) continue;

            long targetAmount = configStack.amount();
            GenericStack storedStack = this.storage.getStack(slot);

            long currentAmount = storedStack != null ? storedStack.amount() : 0;
            long canInsert = Math.min(remaining, targetAmount - currentAmount);

            if (canInsert > 0) {
                if (mode == Actionable.MODULATE) {
                    GenericStack insertStack = new GenericStack(what, canInsert);
                    if (storedStack == null) {
                        this.storage.setStack(slot, insertStack);
                    } else {
                        this.storage.setStack(slot, new GenericStack(storedStack.what(), storedStack.amount() + canInsert));
                    }
                }
                remaining -= canInsert;
            }
        }

        if (remaining > 0 && mode == Actionable.MODULATE) {
            leftoversToInsert.add(new GenericStack(what, remaining));
        }
        return amount;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (link.isCanceled() || link.isDone()) {
            this.craftingLinks.remove(link);
        }
        if (link.isCanceled()){
            LogUtils.getLogger().info("Job was cancelled");
            var iterator = craftingLinks.iterator();
            while (iterator.hasNext()){
                iterator.next().cancel();
                iterator.remove();
            }
            flushInv();
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        checkAndExport();
        if (craftingLinks.isEmpty()){
            doesWait = false;
        }
        if (craftingLinks.isEmpty() && toCraftPlans.isEmpty()){
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(EjectorBlock.ISCRAFTING, false));
            return TickRateModulation.IDLE;
        }
        if (!leftoversToInsert.isEmpty()) {
            if (getGridNode() == null) return TickRateModulation.IDLE;
            var storage = getGridNode().getGrid().getStorageService().getInventory();
            var source = IActionSource.ofMachine(this);

            leftoversToInsert.removeIf(stack -> {
                long inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, source);
                return inserted == stack.amount();
            });
        }
        if (craftingLinks.isEmpty()) {
            Iterator<Future<ICraftingPlan>> iterator = toCraftPlans.iterator();
            while (iterator.hasNext()) {
                Future<ICraftingPlan> craftingPlan = iterator.next();
                if (craftingPlan.isDone()) {
                    try {
                        if (getGridNode() == null) return TickRateModulation.IDLE;

                        var result = getGridNode().getGrid().getCraftingService().submitJob(
                                craftingPlan.get(), this, null, true, IActionSource.ofMachine(this)
                        );

                        if (result.successful() && result.link() != null) {
                            this.craftingLinks.add(result.link());
                            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(EjectorBlock.ISCRAFTING, true));
                            this.cantCraft = null;
                            if (this.menu != null){
                                this.menu.cantCraft = "nothing";
                            }
                            iterator.remove();
                            break;
                        } else if (!result.successful()){
                            this.cantCraft = craftingPlan.get().finalOutput();
                            if (this.menu != null){
                                this.menu.cantCraft = String.format("%sx %s", this.cantCraft.what().formatAmount(this.cantCraft.amount(), AmountFormat.SLOT), this.cantCraft.what().toString());
                            }
                            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(EjectorBlock.ISCRAFTING, false));
                            iterator.remove();
                            toCraftPlans.clear();
                            for (var link : craftingLinks){
                                link.cancel();
                            }
                            craftingLinks.clear();
                            flushInv();
                        }
                    } catch (Exception e) {
                        LogUtils.getLogger().info("Crafting plan submit error: {}", String.valueOf(e));
                        iterator.remove();
                    }
                }
            }
        }
        return TickRateModulation.IDLE;
    }

    public void checkAndExport() {
        if (getGridNode() == null || getGridNode().getGrid() == null || !getMainNode().isActive()) return;

        BlockState state = this.getBlockState();
        Direction front = state.getValue(BlockStateProperties.FACING);
        BlockPos frontPos = this.getBlockPos().relative(front);
        BlockEntity targetBE = this.getLevel().getBlockEntity(frontPos);

        if (targetBE == null) return;

        var target = PatternProviderTarget.get(
                getLevel(),
                frontPos,
                targetBE,
                front.getOpposite(),
                IActionSource.ofMachine(this)
        );
        if (target == null) return;

        for (int i = 0; i < config.size(); i++) {
            GenericStack configStack = config.getStack(i);
            if (configStack == null) continue;

            GenericStack storedStack = storage.getStack(i);
            if (storedStack == null) return;
            if (!configStack.what().equals(storedStack.what())) return;
            if (storedStack.amount() < configStack.amount()) return;
        }

        List<KeyCounter> filledCounters = new ArrayList<>();

        for (int i = 0; i < config.size(); i++) {
            GenericStack configStack = config.getStack(i);
            GenericStack storedStack = storage.getStack(i);

            if (configStack == null || storedStack == null) continue;
            if (!configStack.what().equals(storedStack.what())) continue;
            if (storedStack.amount() < configStack.amount()) continue;

            KeyCounter counter = new KeyCounter();
            counter.add(configStack.what(), configStack.amount());
            filledCounters.add(counter);
        }

        if (filledCounters.isEmpty()) return;

        KeyCounter[] inputCounters = filledCounters.toArray(new KeyCounter[0]);

        GenericStack[] inputs = new GenericStack[config.size()];
        for (int i = 0; i < config.size(); i++) {
            if (config.getStack(i) == null) continue;
            inputs[i] = new GenericStack(config.getStack(i).what(), config.getStack(i).amount());
        }

        ItemStack patternStack = PatternDetailsHelper.encodeProcessingPattern(inputs, new GenericStack[]{new GenericStack(AEItemKey.of(Items.OAK_BUTTON), 1)});
        if (!(patternStack.getItem() instanceof EncodedPatternItem patternItem)) return;

        IPatternDetails pattern = patternItem.decode(patternStack, getLevel(), false);
        if (pattern == null || !pattern.supportsPushInputsToExternalInventory()) return;

        var grid = getGridNode().getGrid();
        var storageInventory = grid.getStorageService().getInventory();
        var source = IActionSource.ofMachine(this);

        pattern.pushInputsToExternalInventory(inputCounters, (key, amount) -> {
            long inserted = target.insert(key, amount, Actionable.MODULATE);
            if (inserted < amount) {
                long leftover = amount - inserted;
                storageInventory.insert(key, leftover, Actionable.MODULATE, source);
            }
        });
        for (int i = 0; i < config.size(); i++) {
            storage.setStack(i, null);
        }
        doesWait = false;
        flushInv();
    }

    public void setMenu(EjectorMenu ejectorMenu) {
        this.menu = ejectorMenu;
    }
}
