package net.oktawia.crazyae2addons.logic.Signalling;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.capabilities.Capabilities;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.ConfigInventory;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.entities.SignallingInterfaceBE;
import net.oktawia.crazyae2addons.menus.SignallingInterfaceMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SignallingInterfaceLogic implements ICraftingRequester, IUpgradeableObject, IConfigurableObject {
    @Nullable
    private InterfaceInventory localInvHandler;
    @Nullable
    private MEStorage networkStorage;
    public Map<AEKey, Long> configMap = new HashMap<>();

    public record DiffResult(AEKey key, long diff, Set<AEKey> fuzzyGroup) {}

    protected final SignallingInterfaceLogicHost host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    protected final IActionSource interfaceRequestSource;
    private final SignallingInterfaceMultiCraftingTracker craftingTracker;
    private final IUpgradeInventory upgrades;
    private final ConfigManager cm = new ConfigManager(this::onConfigChanged);

    private final GenericStack[] plannedWork;
    private int priority;

    private final ConfigInventory config;

    private boolean hasConfig = false;
    private final ConfigInventory storage;
    private Map<AEKey, Long> beforeInv = new HashMap<>();

    public SignallingInterfaceLogic(IManagedGridNode gridNode, SignallingInterfaceLogicHost host, Item is, int slots) {
        this.host = host;
        this.config = ConfigInventory.configStacks(null, slots, this::onConfigRowChanged, false);
        this.storage = ConfigInventory.storage(slots, this::onStorageChanged);
        this.mainNode = gridNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);
        this.interfaceRequestSource = new InterfaceRequestSource(mainNode::getNode);

        gridNode.addService(ICraftingRequester.class, this);
        this.upgrades = UpgradeInventories.forMachine(is, 3, this::onUpgradesChanged);
        this.craftingTracker = new SignallingInterfaceMultiCraftingTracker(this, slots);
        this.cm.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.plannedWork = new GenericStack[slots];

        getConfig().useRegisteredCapacities();
        getStorage().useRegisteredCapacities();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();
    }

    private void readConfig() {
        this.hasConfig = !this.config.isEmpty();
        updatePlan();
        this.notifyNeighbors();
    }

    public void writeToNBT(CompoundTag tag) {
        this.config.writeToChildTag(tag, "config");
        this.storage.writeToChildTag(tag, "storage");
        this.upgrades.writeToNBT(tag, "upgrades");
        this.cm.writeToNBT(tag);
        this.craftingTracker.writeToNBT(tag);
        tag.putInt("priority", this.priority);
    }

    public void readFromNBT(CompoundTag tag) {
        this.craftingTracker.readFromNBT(tag);
        this.upgrades.readFromNBT(tag, "upgrades");
        this.config.readFromChildTag(tag, "config");
        this.storage.readFromChildTag(tag, "storage");
        this.cm.readFromNBT(tag);
        this.readConfig();
        this.priority = tag.getInt("priority");
    }

    private class Ticker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo(),
                    true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }

            boolean couldDoWork = updateStorage();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    protected final OptionalInt getRequestInterfacePriority(IActionSource src) {
        return src.context(SignallingInterfaceLogic.InterfaceRequestContext.class)
                .map(ctx -> OptionalInt.of(ctx.getPriority()))
                .orElseGet(OptionalInt::empty);
    }

    protected final boolean isSameGrid(IActionSource src) {
        var otherGrid = src.machine().map(IActionHost::getActionableNode).map(IGridNode::getGrid).orElse(null);
        return otherGrid == mainNode.getGrid();
    }

    protected final boolean hasWorkToDo() {
        for (var requiredWork : this.plannedWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        final BlockEntity te = this.host.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
    }

    public void gridChanged() {
        this.networkStorage = mainNode.getGrid().getStorageService().getInventory();

        this.notifyNeighbors();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public ConfigInventory getStorage() {
        return storage;
    }

    public ConfigInventory getConfig() {
        return config;
    }

    public MEStorage getInventory() {
        if (hasConfig) {
            return getLocalInventory();
        }

        return networkStorage;
    }

    private MEStorage getLocalInventory() {
        if (localInvHandler == null) {
            localInvHandler = new InterfaceInventory();
        }
        return localInvHandler;
    }

    private class InterfaceRequestSource extends MachineSource {
        private final InterfaceRequestContext context;

        InterfaceRequestSource(IActionHost v) {
            super(v);
            this.context = new InterfaceRequestContext();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            if (key == InterfaceRequestContext.class) {
                return Optional.of(key.cast(this.context));
            }

            return super.context(key);
        }
    }

    private class InterfaceRequestContext {
        public int getPriority() {
            return priority;
        }
    }

    private boolean updateStorage() {
        boolean didSomething = false;

        for (int x = 0; x < plannedWork.length; x++) {
            var work = plannedWork[x];
            if (work != null) {
                var amount = (int) work.amount();
                didSomething = this.usePlan(x, work.what(), amount) || didSomething;
            }
        }

        return didSomething;
    }

    private boolean usePlan(int x, AEKey what, int amount) {
        boolean changed = tryUsePlan(x, what, amount);

        if (changed) {
            this.updatePlan(x);
        }

        return changed;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        int slot = this.craftingTracker.getSlot(link);
        return storage.insert(slot, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    private void updatePlan() {
        var hadWork = this.hasWorkToDo();
        for (int x = 0; x < this.config.size(); x++) {
            this.updatePlan(x);
        }
        var hasWork = this.hasWorkToDo();

        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    private void updatePlan(int slot) {
        var req = this.config.getStack(slot);
        var stored = this.storage.getStack(slot);

        if (req == null && stored != null) {
            this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
        } else if (req != null) {
            if (stored == null) {
                // Nothing stored, request from network
                this.plannedWork[slot] = req;
            } else if (storedRequestEquals(req.what(), stored.what())) {
                if (req.amount() != stored.amount()) {
                    // Already correct type, but incorrect amount, equilize the difference
                    this.plannedWork[slot] = new GenericStack(req.what(), req.amount() - stored.amount());
                } else {
                    this.plannedWork[slot] = null;
                }
            } else {
                // Requested item differs from stored -> push back into storage before fulfilling request
                this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
            }
        } else {
            // Slot matches desired state
            this.plannedWork[slot] = null;
        }
    }

    private boolean storedRequestEquals(AEKey request, AEKey stored) {
        if (upgrades.isInstalled(AEItems.FUZZY_CARD) && request.supportsFuzzyRangeSearch()) {
            return request.fuzzyEquals(stored, cm.getSetting(Settings.FUZZY_MODE));
        } else {
            return request.equals(stored);
        }
    }

    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        var networkInv = grid.getStorageService().getInventory();
        var energySrc = grid.getEnergyService();

        // Always move out unwanted items before handling crafting or restocking
        if (amount < 0) {
            // Move from interface to network storage
            amount = -amount;

            // Make sure the storage has enough items to execute the plan
            var inSlot = storage.getStack(slot);
            if (!what.matches(inSlot) || inSlot.amount() < amount) {
                return true; // Replan
            }

            var inserted = (int) StorageHelper.poweredInsert(energySrc, networkInv, what, amount,
                    this.interfaceRequestSource);

            // Remove the items we just injected somewhere else into the network.
            if (inserted > 0) {
                storage.extract(slot, what, inserted, Actionable.MODULATE);
            }

            return inserted > 0;
        }

        if (this.craftingTracker.isBusy(slot)) {
            // We are already waiting for a crafting result for this slot
            return this.handleCrafting(slot, what, amount);
        } else if (amount > 0) {
            // Move from network into interface
            // Ensure the plan isn't outdated
            if (storage.insert(slot, what, amount, Actionable.SIMULATE) != amount) {
                return true;
            }

            // Try to pull the exact item
            if (acquireFromNetwork(energySrc, networkInv, slot, what, amount)) {
                return true;
            }

            // Try a fuzzy import from network instead if we don't have stacks in stock yet
            if (storage.getStack(slot) == null && upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                FuzzyMode fuzzyMode = getConfigManager().getSetting(Settings.FUZZY_MODE);
                for (var entry : grid.getStorageService().getCachedInventory().findFuzzy(what, fuzzyMode)) {
                    // Simulate insertion first in case the stack size is different
                    long maxAmount = storage.insert(slot, entry.getKey(), amount, Actionable.SIMULATE);
                    if (acquireFromNetwork(energySrc, networkInv, slot, entry.getKey(), maxAmount)) {
                        return true;
                    }
                }
            }

            return this.handleCrafting(slot, what, amount);
        }

        // else wtf?
        return false;
    }

    private boolean acquireFromNetwork(IEnergyService energySrc, MEStorage networkInv, int slot, AEKey what,
                                       long amount) {
        var acquired = StorageHelper.poweredExtraction(energySrc, networkInv, what, amount,
                this.interfaceRequestSource);
        if (acquired > 0) {
            var inserted = storage.insert(slot, what, acquired, Actionable.MODULATE);
            if (inserted < acquired) {
                throw new IllegalStateException("bad attempt at managing inventory. Voided items: " + inserted);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean handleCrafting(int x, AEKey key, long amount) {
        var grid = mainNode.getGrid();
        if (grid != null && upgrades.isInstalled(AEItems.CRAFTING_CARD) && key != null) {
            return this.craftingTracker.handleCrafting(x, key, amount,
                    this.host.getBlockEntity().getLevel(),
                    grid.getCraftingService(),
                    this.actionSource);
        }

        return false;
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    private void onConfigChanged() {
        this.host.saveChanges();
        updatePlan(); // update plan in case fuzzy mode changed
    }

    private void onUpgradesChanged() {
        this.host.saveChanges();

        if (!upgrades.isInstalled(AEItems.CRAFTING_CARD)) {
            // Cancel crafting if the crafting card is removed
            this.cancelCrafting();
        }

        // Update plan in case fuzzy card was inserted or removed
        updatePlan();
    }

    private void onConfigRowChanged() {
        this.configMap.clear();
        for (int i = 0; i < this.config.size(); i++){
            var what = this.config.getKey(i);
            var amount = this.config.getAmount(i);
            if (what != null){
                this.configMap.put(what, amount);
            }
        }
        this.host.saveChanges();
        this.readConfig();
    }

    public DiffResult compare(Map<AEKey, Long> newStacks) {
        Set<AEKey> allKeys = getAllKeys(newStacks);
        boolean hasFuzzyUpgrade = isUpgradedWith(AEItems.FUZZY_CARD);
        FuzzyMode effectiveFuzzyMode = hasFuzzyUpgrade
                ? getConfigManager().getSetting(Settings.FUZZY_MODE)
                : FuzzyMode.IGNORE_ALL;

        return hasFuzzyUpgrade
                ? compareFuzzy(allKeys, newStacks, effectiveFuzzyMode)
                : compareStandard(allKeys, newStacks);
    }

    private Set<AEKey> getAllKeys(Map<AEKey, Long> newStacks) {
        Set<AEKey> allKeys = new HashSet<>(beforeInv.keySet());
        allKeys.addAll(newStacks.keySet());
        return allKeys;
    }

    private DiffResult compareStandard(Set<AEKey> allKeys, Map<AEKey, Long> newStacks) {
        DiffResult result = new DiffResult(null, 0L, null);
        for (AEKey key : allKeys) {
            long diff = newStacks.getOrDefault(key, 0L) - beforeInv.getOrDefault(key, 0L);
            if (diff != 0 && (result.key == null || Math.abs(diff) > Math.abs(result.diff))) {
                result = new DiffResult(key, diff, null);
            }
        }
        return result;
    }

    private DiffResult compareFuzzy(Set<AEKey> allKeys, Map<AEKey, Long> newStacks, FuzzyMode mode) {
        DiffResult result = new DiffResult(null, 0L, null);
        List<Set<AEKey>> groups = groupKeysByFuzzy(allKeys, mode);

        for (Set<AEKey> group : groups) {
            long sumBefore = group.stream().mapToLong(k -> beforeInv.getOrDefault(k, 0L)).sum();
            long sumNew = group.stream().mapToLong(k -> newStacks.getOrDefault(k, 0L)).sum();
            long diff = sumNew - sumBefore;

            if (diff != 0) {
                AEKey keyForDiffResult = group.stream()
                        .filter(newStacks::containsKey)
                        .findFirst()
                        .orElseGet(() -> group.iterator().next());
                if (result.key == null || Math.abs(diff) > Math.abs(result.diff)) {
                    result = new DiffResult(keyForDiffResult, diff, group);
                }
            }
        }
        return result;
    }

    private List<Set<AEKey>> groupKeysByFuzzy(Set<AEKey> keys, FuzzyMode mode) {
        List<Set<AEKey>> groups = new ArrayList<>();

        for (AEKey key : keys) {
            boolean added = false;
            for (Set<AEKey> group : groups) {
                if (group.iterator().next().fuzzyEquals(key, mode)) {
                    group.add(key);
                    added = true;
                    break;
                }
            }
            if (!added) {
                Set<AEKey> newGroup = new HashSet<>();
                newGroup.add(key);
                groups.add(newGroup);
            }
        }
        return groups;
    }


    private void onStorageChanged() {
        Map<AEKey, Long> newInv = updateInventoryFromStorage();
        if (isUpgradedWith(AEItems.REDSTONE_CARD)) {
            var diffResult = compare(newInv);
            if (diffResult.key != null && diffResult.diff > 0 && !getLocation().getLevel().isClientSide) {
                AEKey configKey = extractConfigKey(diffResult);
                if (hasValidGridNode() && configKey != null) {
                    if (!isUpgradedWith(AEItems.INVERTER_CARD) && diffResult.diff >= configMap.get(configKey)) {
                        triggerRedstonePulse();
                    }
                } else if (isUpgradedWith(AEItems.INVERTER_CARD)) {
                    triggerRedstonePulse();
                }
            }
        }
        beforeInv = updateInventoryFromStorage();
        host.saveChanges();
        updatePlan();
    }

    private Map<AEKey, Long> updateInventoryFromStorage() {
        Map<AEKey, Long> inv = new HashMap<>();
        for (int i = 0; i < storage.size(); i++) {
            var stack = storage.getStack(i);
            if (stack != null) {
                inv.put(stack.what(), stack.amount());
            }
        }
        return inv;
    }

    private AEKey extractConfigKey(DiffResult diffResult) {
        AEKey configKey = null;
        if (diffResult.fuzzyGroup != null && isUpgradedWith(AEItems.FUZZY_CARD)) {
            for (AEKey fuzzyKey : diffResult.fuzzyGroup) {
                configKey = configMap.keySet().stream().filter(
                        key -> key.fuzzyEquals(fuzzyKey, getConfigManager().getSetting(Settings.FUZZY_MODE))
                ).findAny().orElse(null);
                if (configKey != null) {
                    break;
                }
            }
        } else if (configMap.containsKey(diffResult.key)) {
            configKey = diffResult.key;
        }
        return configKey;
    }

    private boolean hasValidGridNode() {
        var blockEntity = host.getBlockEntity();
        var gridNode = ((SignallingInterfaceBE) blockEntity).getGridNode();
        return gridNode != null && gridNode.isPowered();
    }

    private void triggerRedstonePulse() {
        var blockEntity = host.getBlockEntity();
        SignallingInterfaceBE signallingBlock = (SignallingInterfaceBE) blockEntity;
        signallingBlock.redstoneOut = true;
        blockEntity.getLevel().updateNeighborsAt(
                blockEntity.getBlockPos(), blockEntity.getBlockState().getBlock());
        Runnable revert = () -> {
            signallingBlock.redstoneOut = false;
            blockEntity.getLevel().updateNeighborsAt(
                    blockEntity.getBlockPos(), blockEntity.getBlockState().getBlock());
        };
        Utils.asyncDelay(revert, 0.05f);
    }

    public void addDrops(List<ItemStack> drops) {
        for (var is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

        for (int i = 0; i < this.storage.size(); i++) {
            var stack = storage.getStack(i);

            if (stack != null) {
                stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                        this.host.getBlockEntity().getBlockPos());
            }
        }
    }

    public void clearContent() {
        this.upgrades.clear();
        this.storage.clear();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    private class InterfaceInventory extends DelegatingMEInventory {
        InterfaceInventory() {
            super(storage);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (getRequestInterfacePriority(source).isPresent() && isSameGrid(source)) {
                return 0;
            }

            return super.insert(what, amount, mode, source);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            var requestPriority = getRequestInterfacePriority(source);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority() && isSameGrid(source)) {
                return 0;
            }

            return super.extract(what, amount, mode, source);
        }

        @Override
        public Component getDescription() {
            return host.getMainMenuIcon().getHoverName();
        }
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass, Direction facing) {
        if (capabilityClass == Capabilities.GENERIC_INTERNAL_INV) {
            return LazyOptional.of(this::getStorage).cast();
        } else if (capabilityClass == Capabilities.STORAGE) {
            return LazyOptional.of(this::getInventory).cast();
        } else {
            return LazyOptional.empty();
        }
    }

    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SignallingInterfaceMenu(i, inventory, this.host);
    }
}
