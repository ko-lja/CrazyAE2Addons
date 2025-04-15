package net.oktawia.crazyae2addons.logic.Circuited;

import java.util.*;

import appeng.api.parts.IPart;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.helpers.patternprovider.*;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.misc.InterfacePart;
import appeng.parts.storagebus.StorageBusPart;
import com.google.common.util.concurrent.Runnables;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.minecraft.core.BlockPos;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Items;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.PlayerInternalInventory;
import stone.mae2.parts.p2p.PatternP2PTunnelPart;

public class CircuitedPatternProviderLogic extends PatternProviderLogic implements InternalInventoryHost, ICraftingProvider {

    public static final String NBT_MEMORY_CARD_PATTERNS = "patterns";
    public static final String NBT_UNLOCK_EVENT = "unlockEvent";
    public static final String NBT_UNLOCK_STACK = "unlockStack";
    public static final String NBT_PRIORITY = "priority";
    public static final String NBT_SEND_LIST = "sendList";
    public static final String NBT_SEND_DIRECTION = "sendDirection";
    public static final String NBT_RETURN_INV = "returnInv";

    private final CircuitedPatternProviderLogicHost host;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final ConfigManager configManager = new ConfigManager(this::configChanged);

    private int priority;

    // Pattern storing logic
    private final AppEngInternalInventory patternInventory;
    private final List<IPatternDetails> patterns = new ArrayList<>();
    private final Set<AEKey> patternInputs = new HashSet<>();
    // Pattern sending logic
    private final List<GenericStack> sendList = new ArrayList<>();
    private Direction sendDirection;
    // Stack returning logic
    private final PatternProviderReturnInventory returnInv;

    private final CircuitedPatternProviderTargetCache[] targetCaches = new CircuitedPatternProviderTargetCache[6];

    private YesNo redstoneState = YesNo.UNDECIDED;

    @Nullable
    private UnlockCraftingEvent unlockEvent;
    @Nullable
    private GenericStack unlockStack;
    private int roundRobinIndex = 0;

    public CircuitedPatternProviderLogic(IManagedGridNode mainNode, CircuitedPatternProviderLogicHost host, int patternInventorySize) {
        super(mainNode, host, patternInventorySize);
        this.patternInventory = new AppEngInternalInventory(this, patternInventorySize);
        this.host = host;
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker())
                .addService(ICraftingProvider.class, this);
        this.actionSource = new MachineSource(mainNode::getNode);

        this.configManager.registerSetting(Settings.BLOCKING_MODE, YesNo.NO);
        this.configManager.registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES);
        this.configManager.registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);

        this.returnInv = new PatternProviderReturnInventory(() -> {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
            this.host.saveChanges();
        });
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();

        ICraftingProvider.requestUpdate(mainNode);
    }

    public void writeToNBT(CompoundTag tag) {
        this.configManager.writeToNBT(tag);
        this.patternInventory.writeToNBT(tag, NBT_MEMORY_CARD_PATTERNS);
        tag.putInt(NBT_PRIORITY, this.priority);
        if (unlockEvent == UnlockCraftingEvent.REDSTONE_POWER) {
            tag.putByte(NBT_UNLOCK_EVENT, (byte) 1);
        } else if (unlockEvent == UnlockCraftingEvent.RESULT) {
            if (unlockStack != null) {
                tag.putByte(NBT_UNLOCK_EVENT, (byte) 2);
                tag.put(NBT_UNLOCK_STACK, GenericStack.writeTag(unlockStack));
            }
        } else if (unlockEvent == UnlockCraftingEvent.REDSTONE_PULSE) {
            tag.putByte(NBT_UNLOCK_EVENT, (byte) 3);
        }

        ListTag sendListTag = new ListTag();
        for (var toSend : sendList) {
            sendListTag.add(GenericStack.writeTag(toSend));
        }
        tag.put(NBT_SEND_LIST, sendListTag);
        if (sendDirection != null) {
            tag.putByte(NBT_SEND_DIRECTION, (byte) sendDirection.get3DDataValue());
        }

        tag.put(NBT_RETURN_INV, this.returnInv.writeToTag());
    }

    public void readFromNBT(CompoundTag tag) {
        this.configManager.readFromNBT(tag);
        this.patternInventory.readFromNBT(tag, NBT_MEMORY_CARD_PATTERNS);
        this.priority = tag.getInt(NBT_PRIORITY);

        var unlockEventType = tag.getByte(NBT_UNLOCK_EVENT);
        this.unlockEvent = switch (unlockEventType) {
            case 0 -> null;
            case 1 -> UnlockCraftingEvent.REDSTONE_POWER;
            case 2 -> UnlockCraftingEvent.RESULT;
            case 3 -> UnlockCraftingEvent.REDSTONE_PULSE;
            default -> null;
        };
        if (this.unlockEvent == UnlockCraftingEvent.RESULT) {
            this.unlockStack = GenericStack.readTag(tag.getCompound(NBT_UNLOCK_STACK));
        } else {
            this.unlockStack = null;
        }

        var sendListTag = tag.getList("sendList", Tag.TAG_COMPOUND);
        for (int i = 0; i < sendListTag.size(); ++i) {
            var stack = GenericStack.readTag(sendListTag.getCompound(i));
            if (stack != null) {
                this.addToSendList(stack.what(), stack.amount());
            }
        }
        if (tag.contains("sendDirection")) {
            sendDirection = Direction.from3DDataValue(tag.getByte("sendDirection"));
        }

        this.returnInv.readFromTag(tag.getList("returnInv", Tag.TAG_COMPOUND));
    }

    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.saveChanges();
        this.updatePatterns();
    }

    @Override
    public boolean isClientSide() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void updatePatterns() {
        patterns.clear();
        patternInputs.clear();

        for (var stack : this.patternInventory) {
            var details = PatternDetailsHelper.decodePattern(stack, this.host.getBlockEntity().getLevel());

            if (details != null) {
                patterns.add(details);

                for (var iinput : details.getInputs()) {
                    for (var inputCandidate : iinput.getPossibleInputs()) {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public int getPatternPriority() {
        return this.priority;
    }

    public static void setCirc(int circ, BlockPos pos, Level lvl){
        var machine = SimpleTieredMachine.getMachine(lvl, pos);
        NotifiableItemStackHandler inv;
        if (machine instanceof SimpleTieredMachine STM){
            inv = STM.getCircuitInventory();
        } else if (machine instanceof ItemBusPartMachine IBPM) {
            inv = IBPM.getCircuitInventory();
        } else if (machine instanceof FluidHatchPartMachine FHPM) {
            inv = FHPM.getCircuitInventory();
        } else {
            return;
        }
        if (circ == 0){
            inv.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            var machineStack = GTItems.PROGRAMMED_CIRCUIT.asStack();
            IntCircuitBehaviour.setCircuitConfiguration(machineStack, circ);
            inv.setStackInSlot(0, machineStack);
        }
    }

    public ICircuitedPatternProviderTarget getAdapter(Level lvl, BlockPos pos, Direction dir){
        return new CircuitedPatternProviderTargetCache(lvl.getServer().getLevel(lvl.dimension()), pos, dir, actionSource).find();
    }

    @Nullable
    private ICircuitedPatternProviderTarget findAdapter(Direction side) {
        if (side == null) return null;
        if (targetCaches[side.get3DDataValue()] == null) {
            var thisBe = host.getBlockEntity();
            targetCaches[side.get3DDataValue()] = new CircuitedPatternProviderTargetCache(
                    (ServerLevel) thisBe.getLevel(),
                    thisBe.getBlockPos().relative(side),
                    side.getOpposite(),
                    actionSource);
        }

        return targetCaches[side.get3DDataValue()].find();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();

        if (getCraftingLockedReason() != LockCraftingMode.NONE) {
            return false;
        }

        record PushTarget(Direction direction, ICircuitedPatternProviderTarget target) {}

        var possibleTargets = new ArrayList<PushTarget>();

        for (var direction : getActiveSides()) {
            var adapter = findAdapter(direction);
            var adapterBE = level.getBlockEntity(host.getBlockEntity().getBlockPos().relative(direction));
            IPart adapterPart;
            if (adapterBE instanceof CableBusBlockEntity cbus) {
                adapterPart = cbus.getPart(direction.getOpposite());
            } else {
                adapterPart = null;
            }
            if (adapter == null || adapterPart instanceof PatternP2PTunnelPart){
                var target = level.getBlockEntity(host.getBlockEntity().getBlockPos().relative(direction));
                if (target instanceof CableBusBlockEntity cbus){
                    var part = cbus.getPart(direction.getOpposite());
                    if (part instanceof PatternP2PTunnelPart pp2p){
                        pp2p.getOutputs().forEach(out -> {
                            var tg = out.getLevel().getBlockEntity(out.getBlockEntity().getBlockPos().relative(out.getSide()));
                            if (tg != null){
                                if (tg instanceof CableBusBlockEntity cbus1){
                                    var part1 = cbus1.getPart(out.getSide().getOpposite());
                                    if (part1 instanceof InterfacePart ipart) {
                                        var adapter2 = getAdapter(ipart.getLevel(), ipart.getBlockEntity().getBlockPos(), ipart.getSide());
                                        if (adapter2 != null){
                                            possibleTargets.add(new PushTarget(direction, adapter2));
                                        }
                                    }
                                } else {
                                    var adapter1 = getAdapter(out.getLevel(), tg.getBlockPos(), out.getSide());
                                    if (adapter1 != null){
                                        possibleTargets.add(new PushTarget(direction, adapter1));
                                    }
                                }
                            }
                        });
                    }
                }
            } else {
                possibleTargets.add(new PushTarget(direction, adapter));
            }
        }

        List<PushTarget> rotated = Utils.rotate(possibleTargets, roundRobinIndex);
        possibleTargets.clear();
        possibleTargets.addAll(rotated);

        for (var target : possibleTargets) {
            var direction = target.direction();
            var adapter = target.target();

            if (this.isBlocking()) {
                if (adapter.containsPatternInput(this.patternInputs)){
                    continue;
                } else {
                    var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
                    ArrayList<AEKey> frozenInvContents = new ArrayList<>();
                    var targetedInvHost = adapter.parent().level.getBlockEntity(adapter.parent().pos);
                    MEStorage storage = null;
                    boolean doSkip = false;
                    if (targetedInvHost != null){
                        if (targetedInvHost instanceof CableBusBlockEntity cbus){
                            if (cbus.getPart(adapter.parent().direction) instanceof InterfacePart IP){
                                IP.getGridNode()
                                .getGrid()
                                .getMachines(StorageBusPart.class)
                                .stream()
                                .filter(
                                    sbp ->
                                        SimpleTieredMachine.getMachine(sbp.getLevel(), sbp.getHost().getBlockEntity().getBlockPos().relative(sbp.getSide())) != null &&
                                        sbp.isUpgradedWith(Items.CIRCUIT_UPGRADE_CARD_ITEM))
                                .forEach(sbp -> {
                                    var realTarget = sbp.getLevel().getBlockEntity(sbp.getBlockEntity().getBlockPos().relative(sbp.getSide()));
                                    if (realTarget != null){
                                        for (var entry : StackWorldBehaviors.createExternalStorageStrategies(
                                                realTarget.getLevel().getServer().getLevel(realTarget.getLevel().dimension()),
                                                realTarget.getBlockPos(),
                                                adapter.parent().direction).entrySet()) {
                                            var wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
                                            if (wrapper != null) {
                                                externalStorages.put(entry.getKey(), wrapper);
                                                wrapper.getAvailableStacks().forEach(stack -> {
                                                    frozenInvContents.add(stack.getKey());
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            storage = targetedInvHost.getCapability(Capabilities.STORAGE, adapter.parent().direction).orElse(null);
                        }
                        if (storage == null) {
                            if (frozenInvContents.isEmpty()){
                                var strategies = StackWorldBehaviors.createExternalStorageStrategies(adapter.parent().level, adapter.parent().pos, adapter.parent().direction);
                                for (var entry : strategies.entrySet()) {
                                    var wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
                                    if (wrapper != null) {
                                        externalStorages.put(entry.getKey(), wrapper);
                                        wrapper.getAvailableStacks().forEach(stack -> {
                                            frozenInvContents.add(stack.getKey());
                                        });
                                    }
                                }
                            }
                        }
                        for (AEKey key : frozenInvContents) {
                            if (patternInputs.contains(key.dropSecondary())) {
                                doSkip = true;
                            }
                        }
                    }
                    if (doSkip) {
                        continue;
                    }
                }
            }

            if (adapter == null) continue;
            if (this.adapterAcceptsAll(adapter, inputHolder)) {
                var tag = patternDetails.getDefinition().getTag();
                int circ;
                if (tag != null && tag.contains("circuit")){
                    circ = tag.getInt("circuit");
                } else {
                    circ = 0;
                }

                boolean canWork = false;
                var machine = SimpleTieredMachine.getMachine(adapter.parent().level, adapter.parent().pos);
                if (machine == null){
                    var mLvl = adapter.parent().level;
                    if (mLvl.getBlockEntity(adapter.parent().pos) instanceof CableBusBlockEntity CBBE){
                        var f = CBBE.getPart(adapter.parent().direction);
                        if (CBBE.getPart(adapter.parent().direction) instanceof InterfacePart IP){
                            if (IP.getGridNode().getGrid().getMachines(StorageBusPart.class)
                                .stream()
                                .allMatch(
                                bus -> SimpleTieredMachine.getMachine(bus.getLevel(),
                                        bus.getBlockEntity().getBlockPos().relative(bus.getSide())) != null &&
                                        bus.isUpgradedWith(Items.CIRCUIT_UPGRADE_CARD_ITEM))){
                                canWork = true;
                            };
                        }
                    }
                } else {
                    canWork = true;
                }

                if (!canWork){
                    continue;
                }

                setCirc(circ, adapter.parent().pos, adapter.parent().level);

                if (adapter.parent().level.getBlockEntity(adapter.parent().pos) instanceof CableBusBlockEntity CBBE){
                    if (CBBE.getPart(adapter.parent().direction) instanceof InterfacePart IP){
                        IP.getGridNode().getGrid().getMachines(StorageBusPart.class).forEach(bus -> {
                            if (bus.isUpgradedWith(Items.CIRCUIT_UPGRADE_CARD_ITEM)){
                                setCirc(circ, bus.getBlockEntity().getBlockPos().relative(bus.getSide()), bus.getLevel());
                            }
                        });
                    }
                }

                patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                    var inserted = adapter.insert(what, amount, Actionable.MODULATE);
                    if (inserted < amount) {
                        this.addToSendList(what, amount - inserted);
                    }
                });
                onPushPatternSuccess(patternDetails);
                this.sendDirection = direction;
                this.sendStacksOut(adapter);
                ++roundRobinIndex;
                return true;
            }
        }

        return false;
    }

    public void resetCraftingLock() {
        if (unlockEvent != null) {
            unlockEvent = null;
            unlockStack = null;
            saveChanges();
        }
    }

    private void onPushPatternSuccess(IPatternDetails pattern) {
        resetCraftingLock();

        var lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        switch (lockMode) {
            case LOCK_UNTIL_PULSE -> {
                if (getRedstoneState()) {
                    // Already have signal, wait for no signal before switching to REDSTONE_POWER
                    unlockEvent = UnlockCraftingEvent.REDSTONE_PULSE;
                } else {
                    // No signal, wait for signal
                    unlockEvent = UnlockCraftingEvent.REDSTONE_POWER;
                }
                redstoneState = YesNo.UNDECIDED; // Check redstone state again next update
                saveChanges();
            }
            case LOCK_UNTIL_RESULT -> {
                unlockEvent = UnlockCraftingEvent.RESULT;
                unlockStack = pattern.getPrimaryOutput();
                saveChanges();
            }
        }
    }

    public LockCraftingMode getCraftingLockedReason() {
        var lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !getRedstoneState()) {
            // Crafting locked by redstone signal
            return LockCraftingMode.LOCK_WHILE_LOW;
        } else if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && getRedstoneState()) {
            return LockCraftingMode.LOCK_WHILE_HIGH;
        } else if (unlockEvent != null) {
            // Crafting locked by waiting for unlock event
            switch (unlockEvent) {
                case REDSTONE_POWER, REDSTONE_PULSE -> {
                    return LockCraftingMode.LOCK_UNTIL_PULSE;
                }
                case RESULT -> {
                    return LockCraftingMode.LOCK_UNTIL_RESULT;
                }
            }
        }
        return LockCraftingMode.NONE;
    }

    @Nullable
    public GenericStack getUnlockStack() {
        return unlockStack;
    }

    private Set<Direction> getActiveSides() {
        var sides = host.getTargets();

        // Skip sides with grid connections to other pattern providers and to interfaces connected to the same network
        var node = mainNode.getNode();
        if (node != null) {
            for (var entry : node.getInWorldConnections().entrySet()) {
                var otherNode = entry.getValue().getOtherSide(node);
                if (otherNode.getOwner() instanceof PatternProviderLogicHost
                        || (otherNode.getOwner() instanceof InterfaceLogicHost
                        && otherNode.getGrid().equals(mainNode.getGrid()))) {
                    sides.remove(entry.getKey());
                }
            }
        }

        return sides;
    }

    public boolean isBlocking() {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }


    private boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
        for (var inputList : inputHolder) {
            for (var input : inputList) {
                var inserted = target.insert(input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addToSendList(AEKey what, long amount) {
        if (amount > 0) {
            this.sendList.add(new GenericStack(what, amount));

            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    private boolean sendStacksOut(PatternProviderTarget where) {
        if (sendDirection == null) {
            if (!sendList.isEmpty()) {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }

        if (where == null) {
            return false;
        }

        boolean didSomething = false;

        for (var it = sendList.listIterator(); it.hasNext();) {
            var stack = it.next();
            var what = stack.what();
            long amount = stack.amount();

            var inserted = where.insert(what, amount, Actionable.MODULATE);
            if (inserted >= amount) {
                it.remove();
                didSomething = true;
            } else if (inserted > 0) {
                it.set(new GenericStack(what, amount - inserted));
                didSomething = true;
            }
        }

        if (sendList.isEmpty()) {
            sendDirection = null;
        }

        return didSomething;
    }

    @Override
    public boolean isBusy() {
        return !sendList.isEmpty();
    }

    private boolean hasWorkToDo() {
        return !sendList.isEmpty() || !returnInv.isEmpty();
    }

    private boolean doWork() {
        // Note: bitwise OR to avoid short-circuiting.
        return returnInv.injectIntoNetwork(
                mainNode.getGrid().getStorageService().getInventory(), actionSource, this::onStackReturnedToNetwork)
                | sendStacksOut(findAdapter(sendDirection));
    }

    public InternalInventory getPatternInv() {
        return this.patternInventory;
    }

    public void onMainNodeStateChanged() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
        }
    }

    public void addDrops(List<ItemStack> drops) {
        for (var stack : this.patternInventory) {
            drops.add(stack);
        }

        for (var stack : this.sendList) {
            stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                    this.host.getBlockEntity().getBlockPos());
        }

        this.returnInv.addDrops(drops, this.host.getBlockEntity().getLevel(), this.host.getBlockEntity().getBlockPos());
    }

    public void clearContent() {
        this.patternInventory.clear();
        this.sendList.clear();
        this.returnInv.clear();
    }

    public PatternProviderReturnInventory getReturnInv() {
        return this.returnInv;
    }

    public void exportSettings(CompoundTag output) {
        patternInventory.writeToNBT(output, NBT_MEMORY_CARD_PATTERNS);
    }

    public void importSettings(CompoundTag input, @Nullable Player player) {
        if (player != null && input.contains(NBT_MEMORY_CARD_PATTERNS) && !player.level().isClientSide) {
            clearPatternInventory(player);

            var desiredPatterns = new AppEngInternalInventory(patternInventory.size());
            desiredPatterns.readFromNBT(input, NBT_MEMORY_CARD_PATTERNS);

            // Restore from blank patterns in the player inv
            var playerInv = player.getInventory();
            var blankPatternsAvailable = player.getAbilities().instabuild ? Integer.MAX_VALUE
                    : playerInv.countItem(AEItems.BLANK_PATTERN.asItem());
            var blankPatternsUsed = 0;
            for (int i = 0; i < desiredPatterns.size(); i++) {
                // Don't restore junk
                var pattern = PatternDetailsHelper.decodePattern(desiredPatterns.getStackInSlot(i),
                        host.getBlockEntity().getLevel(), true);
                if (pattern == null) {
                    continue; // Skip junk / broken recipes
                }

                // Keep track of how many blank patterns we need
                ++blankPatternsUsed;
                if (blankPatternsAvailable >= blankPatternsUsed) {
                    if (!patternInventory.addItems(pattern.getDefinition().toStack()).isEmpty()) {
                        AELog.warn("Failed to add pattern to pattern provider");
                        blankPatternsUsed--;
                    }
                }
            }

            // Deduct the used blank patterns
            if (blankPatternsUsed > 0 && !player.getAbilities().instabuild) {
                new PlayerInternalInventory(playerInv)
                        .removeItems(blankPatternsUsed, AEItems.BLANK_PATTERN.stack(), null);
            }

            // Warn about not being able to restore all patterns due to lack of blank patterns
            if (blankPatternsUsed > blankPatternsAvailable) {
                player.sendSystemMessage(
                        PlayerMessages.MissingBlankPatterns.text(blankPatternsUsed - blankPatternsAvailable));
            }
        }
    }

    // Converts all patterns in this provider to blank patterns and give them to the player
    private void clearPatternInventory(Player player) {
        // Just clear it for creative mode players
        if (player.getAbilities().instabuild) {
            for (int i = 0; i < patternInventory.size(); i++) {
                patternInventory.setItemDirect(i, ItemStack.EMPTY);
            }
            return;
        }

        var playerInv = player.getInventory();

        // Clear out any existing patterns and give them to the player
        var blankPatternCount = 0;
        for (int i = 0; i < patternInventory.size(); i++) {
            var pattern = patternInventory.getStackInSlot(i);
            // Auto-Clear encoded patterns to allow them to stack
            if (pattern.is(AEItems.CRAFTING_PATTERN.asItem())
                    || pattern.is(AEItems.PROCESSING_PATTERN.asItem())
                    || pattern.is(AEItems.SMITHING_TABLE_PATTERN.asItem())
                    || pattern.is(AEItems.STONECUTTING_PATTERN.asItem())
                    || pattern.is(AEItems.BLANK_PATTERN.asItem())) {
                blankPatternCount += pattern.getCount();
            } else {
                // Give back any non-blank-patterns individually
                playerInv.placeItemBackInInventory(pattern);
            }
            patternInventory.setItemDirect(i, ItemStack.EMPTY);
        }

        // Place back the removed blank patterns all at once
        if (blankPatternCount > 0) {
            playerInv.placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(blankPatternCount), false);
        }
    }

    private void onStackReturnedToNetwork(GenericStack genericStack) {
        if (unlockEvent != UnlockCraftingEvent.RESULT) {
            return; // If we're not waiting for the result, we don't care
        }

        if (unlockStack == null) {
            // Actually an error state...
            unlockEvent = null;
        } else if (unlockStack.what().equals(genericStack.what())) {
            var remainingAmount = unlockStack.amount() - genericStack.amount();
            if (remainingAmount <= 0) {
                unlockEvent = null;
                unlockStack = null;
            } else {
                unlockStack = new GenericStack(unlockStack.what(), remainingAmount);
            }
        }
    }

    private class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = doWork();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    public PatternContainerGroup getTerminalGroup() {
        var host = this.host.getBlockEntity();
        var hostLevel = host.getLevel();

        // Prefer own custom name / icon if player has named it
        if (this.host instanceof Nameable nameable && nameable.hasCustomName()) {
            var name = nameable.getCustomName();
            return new PatternContainerGroup(
                    this.host.getTerminalIcon(),
                    name,
                    List.of());
        }

        var sides = getActiveSides();
        var groups = new LinkedHashSet<PatternContainerGroup>(sides.size());
        for (var side : sides) {
            var sidePos = host.getBlockPos().relative(side);
            var group = PatternContainerGroup.fromMachine(hostLevel, sidePos, side.getOpposite());
            if (group != null) {
                groups.add(group);
            }
        }

        // If there's just one group, group by that
        if (groups.size() == 1) {
            return groups.iterator().next();
        }

        List<Component> tooltip = List.of();
        // If there are multiple groups, show that in the tooltip
        if (groups.size() > 1) {
            tooltip = new ArrayList<>();
            tooltip.add(GuiText.AdjacentToDifferentMachines.text().withStyle(ChatFormatting.BOLD));
            for (var group : groups) {
                tooltip.add(group.name());
                for (var line : group.tooltip()) {
                    tooltip.add(Component.literal("  ").append(line));
                }
            }
        }

        // If nothing is adjacent, just use itself
        var hostIcon = this.host.getTerminalIcon();
        return new PatternContainerGroup(
                hostIcon,
                hostIcon.getDisplayName(),
                tooltip);
    }

    public long getSortValue() {
        final BlockEntity te = this.host.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        return this.returnInv.getCapability(capability);
    }

    @Nullable
    public IGrid getGrid() {
        return mainNode.getGrid();
    }

    public void updateRedstoneState() {
        // If we're waiting for a pulse, update immediately
        if (unlockEvent == UnlockCraftingEvent.REDSTONE_POWER && getRedstoneState()) {
            unlockEvent = null; // Unlocked!
            saveChanges();
        } else if (unlockEvent == UnlockCraftingEvent.REDSTONE_PULSE && !getRedstoneState()) {
            unlockEvent = UnlockCraftingEvent.REDSTONE_POWER; // Wait for re-power
            redstoneState = YesNo.UNDECIDED; // Need to re-check signal on next update
            saveChanges();
        } else {
            // Otherwise, just reset back to undecided
            redstoneState = YesNo.UNDECIDED;
        }
    }

    private void configChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.LOCK_CRAFTING_MODE) {
            resetCraftingLock();
        } else {
            saveChanges();
        }
    }

    private boolean getRedstoneState() {
        if (redstoneState == YesNo.UNDECIDED) {
            var be = this.host.getBlockEntity();
            redstoneState = be.getLevel().hasNeighborSignal(be.getBlockPos())
                    ? YesNo.YES
                    : YesNo.NO;
        }
        return redstoneState == YesNo.YES;
    }
}
