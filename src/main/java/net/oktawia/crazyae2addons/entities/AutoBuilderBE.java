package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
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
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.AutoBuilderMenu;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Future;

public class AutoBuilderBE extends AENetworkInvBlockEntity implements IGridTickable, MenuProvider, InternalInventoryHost, IUpgradeableObject, ICraftingRequester {

    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(CrazyBlockRegistrar.AUTO_BUILDER_BLOCK.get(), 7, this::setChanged);
    public Integer delay = 20;
    private BlockPos ghostRenderPos = worldPosition.above().above();
    private List<String> code = new ArrayList<>();
    private int currentInstruction = 0;
    private int tickDelayLeft = 0;
    private boolean isRunning = false;
    public AppEngInternalInventory inventory = new AppEngInternalInventory(this, 2);
    public int redstonePulseTicks = 0;
    public List<Future<ICraftingPlan>> toCraftPlans = new ArrayList<>();
    public List<ICraftingLink> craftingLinks = new ArrayList<>();
    private boolean isCrafting = false;
    private List<GenericStack> toCraft = new ArrayList<>();

    public AutoBuilderBE(BlockPos pos, BlockState state) {
        super(CrazyBlockEntityRegistrar.AUTO_BUILDER_BE.get(), pos, state);
        getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(4)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.AUTO_BUILDER_BLOCK.get().asItem())
                );
        this.inventory.setFilter(new IAEItemFilter() {
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return stack.getItem().equals(CrazyItemRegistrar.BUILDER_PATTERN.get().asItem());
            }
        });
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        var inv = getUpgrades();
        for (var stack : inv) {
            var genericStack = GenericStack.unwrapItemStack(stack);
            if (genericStack != null) {
                genericStack.what().addDrops(
                        genericStack.amount(),
                        drops,
                        level,
                        pos);
            } else {
                drops.add(stack);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return LazyOptional.of(() -> new IItemHandler() {
                @Override
                public int getSlots() {
                    return 2;
                }

                @Override
                public @NotNull ItemStack getStackInSlot(int slot) {
                    return inventory.getStackInSlot(slot);
                }

                @Override
                public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    if (slot == 0 && inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(1).isEmpty() && stack.getItem() == CrazyItemRegistrar.BUILDER_PATTERN.get()) {
                        return inventory.insertItem(0, stack, simulate);
                    }
                    return stack;
                }

                @Override
                public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (slot == 1 && !isRunning) {
                        return inventory.extractItem(1, amount, simulate).copy();
                    }
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }

                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                    return slot == 0 && stack.getItem() == CrazyItemRegistrar.BUILDER_PATTERN.get();
                }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.inventory;
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        this.upgrades.readFromNBT(tag, "upgrades");
        this.currentInstruction = tag.getInt("currentInstruction");
        this.tickDelayLeft = tag.getInt("tickDelayLeft");
        this.isRunning = tag.getBoolean("isRunning");
        if (tag.contains("GhostPos")) {
            this.ghostRenderPos = BlockPos.of(tag.getLong("GhostPos"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.upgrades.writeToNBT(tag, "upgrades");

        tag.putInt("currentInstruction", this.currentInstruction);
        tag.putInt("tickDelayLeft", this.tickDelayLeft);
        tag.putBoolean("isRunning", this.isRunning);
        tag.putLong("GhostPos", ghostRenderPos.asLong());
    }

    private void triggerRedstonePulse() {
        redstonePulseTicks = 1;
        if (!level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("GhostPos", ghostRenderPos.asLong());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("GhostPos")) {
            ghostRenderPos = BlockPos.of(tag.getLong("GhostPos"));
        }
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, be -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("GhostPos", ghostRenderPos.asLong());
            return tag;
        });
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag.contains("GhostPos")) {
            ghostRenderPos = BlockPos.of(tag.getLong("GhostPos"));
        }
    }

    public void setGhostRenderPos(BlockPos pos) {
        this.ghostRenderPos = pos;
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    public BlockPos getGhostRenderPos() {
        return ghostRenderPos;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {

        Iterator<Future<ICraftingPlan>> iterator = toCraftPlans.iterator();
        while (iterator.hasNext()) {
            Future<ICraftingPlan> craftingPlan = iterator.next();
            if (craftingPlan.isDone()) {
                try {
                    if (this.craftingLinks.isEmpty()){
                        if (getGridNode() == null) return TickRateModulation.IDLE;
                        var result = getGridNode().getGrid().getCraftingService().submitJob(
                                craftingPlan.get(), this, null, true, IActionSource.ofMachine(this));
                        if (result.successful() && result.link() != null) {
                            this.craftingLinks.add(result.link());
                            iterator.remove();
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }

        if (!isRunning || code.isEmpty() || isCrafting) {
            return TickRateModulation.URGENT;
        }

        if (inventory.getStackInSlot(0).isEmpty()) {
            isRunning = false;
            setGhostRenderPos(worldPosition.above().above());
            return TickRateModulation.URGENT;
        }

        if (tickDelayLeft > 0) {
            tickDelayLeft -= ticksSinceLastCall;
            return TickRateModulation.URGENT;
        }

        int accelCount = upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
        int maxSteps = (int) Math.max(1, Math.pow(accelCount, 4));

        boolean didWork = false;

        for (int steps = 0; steps < maxSteps && currentInstruction < code.size(); steps++) {
            String inst = code.get(currentInstruction);

            if (inst.startsWith("Z|")) {
                tickDelayLeft = Integer.parseInt(inst.substring(2));
                currentInstruction++;
                return TickRateModulation.URGENT;
            }

            didWork = true;

            switch (inst) {
                case "N" -> setGhostRenderPos(getGhostRenderPos().offset(0, 0, -1));
                case "S" -> setGhostRenderPos(getGhostRenderPos().offset(0, 0, 1));
                case "E" -> setGhostRenderPos(getGhostRenderPos().offset(1, 0, 0));
                case "W" -> setGhostRenderPos(getGhostRenderPos().offset(-1, 0, 0));
                case "U" -> setGhostRenderPos(getGhostRenderPos().offset(0, 1, 0));
                case "D" -> setGhostRenderPos(getGhostRenderPos().offset(0, -1, 0));
                case "R" -> setGhostRenderPos(worldPosition.above().above());
                case "X" -> {
                    var grid = getMainNode().getGrid();
                    if (grid != null) {
                        double dx = getGhostRenderPos().getX() - worldPosition.getX();
                        double dy = getGhostRenderPos().getY() - worldPosition.getY();
                        double dz = getGhostRenderPos().getZ() - worldPosition.getZ();
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        var power = grid.getEnergyService().extractAEPower(Math.pow(distance, 3) / 25, Actionable.MODULATE, PowerMultiplier.ONE);
                        if (power >= Math.pow(distance, 3) / 25){
                            if (isBreakable(level.getBlockState(getGhostRenderPos()), level, getGhostRenderPos())) {
                                var drops = getSilkTouchDrops(level.getBlockState(getGhostRenderPos()), (ServerLevel) level, getGhostRenderPos());
                                long inserted = 0;
                                for (var drop : drops){
                                    inserted += StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), AEItemKey.of(drop.getItem()), 1, IActionSource.ofMachine(this), Actionable.MODULATE);
                                }
                                if (inserted > 0){
                                    getLevel().destroyBlock(getGhostRenderPos(), false);
                                }
                            }
                            if (!getLevel().getFluidState(getGhostRenderPos()).isEmpty()){
                                if (getLevel().getFluidState(getGhostRenderPos()).isSource()){
                                    StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), AEFluidKey.of(getLevel().getFluidState(getGhostRenderPos()).getType()), 1000, IActionSource.ofMachine(this), Actionable.MODULATE);
                                }
                                getLevel().setBlock(getGhostRenderPos(), Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
                default -> {
                    if (inst.startsWith("P|")) {
                        String blockIdRaw = inst.substring(2);

                        try {
                            String blockIdClean;
                            Map<String, String> props = new HashMap<>();
                            int idx = blockIdRaw.indexOf('[');
                            if (idx > 0 && blockIdRaw.endsWith("]")) {
                                blockIdClean = blockIdRaw.substring(0, idx);
                                String propString = blockIdRaw.substring(idx + 1, blockIdRaw.length() - 1);
                                for (String pair : propString.split(",")) {
                                    String[] kv = pair.split("=", 2);
                                    if (kv.length == 2) {
                                        props.put(kv[0], kv[1]);
                                    }
                                }
                            } else {
                                blockIdClean = blockIdRaw;
                            }

                            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockIdClean));
                            if (block != null && block != Blocks.AIR) {
                                var grid = getMainNode().getGrid();
                                if (grid != null) {
                                    BlockPos target = getGhostRenderPos();
                                    double dx = getGhostRenderPos().getX() - worldPosition.getX();
                                    double dy = getGhostRenderPos().getY() - worldPosition.getY();
                                    double dz = getGhostRenderPos().getZ() - worldPosition.getZ();
                                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                                    var power = grid.getEnergyService().extractAEPower(Math.pow(distance, 3) / 25, Actionable.MODULATE, PowerMultiplier.ONE);
                                    if (power >= Math.pow(distance, 3) / 25){
                                        if (isBreakable(level.getBlockState(getGhostRenderPos()), level, getGhostRenderPos())) {
                                            var drops = getSilkTouchDrops(level.getBlockState(getGhostRenderPos()), (ServerLevel) level, getGhostRenderPos());
                                            long inserted = 0;
                                            for (var drop : drops){
                                                inserted += StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), AEItemKey.of(drop.getItem()), 1, IActionSource.ofMachine(this), Actionable.MODULATE);
                                            }
                                            if (inserted <= 0 && !drops.isEmpty()) return TickRateModulation.IDLE;
                                        }

                                        var extracted = StorageHelper.poweredExtraction(
                                                grid.getEnergyService(),
                                                grid.getStorageService().getInventory(),
                                                AEItemKey.of(block.asItem()),
                                                1, IActionSource.ofMachine(this), Actionable.MODULATE);

                                        if (extracted > 0) {
                                            BlockState state = block.defaultBlockState();
                                            if (!props.isEmpty()) {
                                                for (Map.Entry<String, String> entry : props.entrySet()) {
                                                    Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
                                                    if (property != null) {
                                                        state = applyProperty(state, property, entry.getValue());
                                                    }
                                                }
                                            }
                                            level.setBlock(target, state, 3);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            currentInstruction++;
        }

        if (currentInstruction >= code.size()) {
            isRunning = false;
            ItemStack pattern = inventory.getStackInSlot(0);
            if (!pattern.isEmpty()) {
                inventory.setItemDirect(0, ItemStack.EMPTY);
                inventory.setItemDirect(1, pattern.copyWithCount(1));
            }
            setGhostRenderPos(worldPosition.above().above());
            triggerRedstonePulse();
        } else if (didWork) {
            tickDelayLeft = this.delay;
        }
        if (redstonePulseTicks > 0) {
            redstonePulseTicks -= ticksSinceLastCall;
            if (redstonePulseTicks <= 0) {
                if (!level.isClientSide) {
                    level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
                }
            }
        }
        return TickRateModulation.URGENT;
    }

    public static List<ItemStack> getSilkTouchDrops(BlockState state, ServerLevel level, BlockPos pos) {
        ItemStack silkTool = new ItemStack(Items.DIAMOND_PICKAXE);
        silkTool.enchant(Enchantments.SILK_TOUCH, 1);

        var lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.TOOL, silkTool)
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.BLOCK_STATE, state);

        return state.getDrops(lootParams);
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String valueStr) {
        try {
            if (property instanceof BooleanProperty) {
                Boolean boolVal = Boolean.parseBoolean(valueStr);
                return state.setValue((BooleanProperty) property, boolVal);
            }
            if (property instanceof IntegerProperty) {
                Integer intVal = Integer.parseInt(valueStr);
                return state.setValue((IntegerProperty) property, intVal);
            }
            Optional<T> value = property.getValue(valueStr);
            if (value.isPresent()) {
                return state.setValue(property, value.get());
            }
        } catch (Exception ignored) {}
        return state;
    }

    public static boolean isBreakable(BlockState state, Level level, BlockPos pos) {
        return state.getDestroySpeed(level, pos) >= 0;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new AutoBuilderMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.AUTO_BUILDER_MENU.get(), player, locator);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void checkBlocksInStorage(Map<String, Integer> requiredBlocks, @Nullable GenericStack additional) {
        this.toCraft.clear();
        if (getGridNode() == null || getGridNode().getGrid() == null) return;

        var storage = getGridNode().getGrid().getStorageService().getInventory();

        for (Map.Entry<String, Integer> entry : requiredBlocks.entrySet()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getKey().split("\\[")[0]));
            var stack = new ItemStack(block.asItem());
            var key = AEItemKey.of(stack);
            long left = 0;
            try {
                if (additional != null && key.toStack().getItem() == additional.what().wrapForDisplayOrFilter().getItem()){
                    left = storage.getAvailableStacks().get(key) + additional.amount() - entry.getValue();
                } else {
                    left = storage.getAvailableStacks().get(key) - entry.getValue();
                }
            } catch (Exception ignored) {
                LogUtils.getLogger().info(block.toString());
            }

            if (left < 0) {
                this.toCraft.add(new GenericStack(key, Math.abs(left)));
            }
        }
    }

    public void scheduleCrafts() {
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

    public void onRedstoneActivate(@Nullable GenericStack additional) {
        if (getLevel() == null) return;
        if (inventory.getStackInSlot(0).isEmpty() && !inventory.getStackInSlot(1).isEmpty()) {
            inventory.setItemDirect(0, inventory.getStackInSlot(1).copyWithCount(1));
            inventory.setItemDirect(1, ItemStack.EMPTY);
        }
        if (!this.inventory.isEmpty()){
            var tag = inventory.getStackInSlot(0).getOrCreateTag();
            if (tag.contains("code")){
                if (tag.getBoolean("code")){
                    var program = ProgramExpander.expand(tag.getString("program"));
                    if (program.success){
                        code = program.program;
                    }
                }
            }
            if (tag.contains("delay")){
                this.delay = tag.getInt("delay");
            }
        }
        if (this.code.isEmpty()) return;

        checkBlocksInStorage(ProgramExpander.countUsedBlocks(String.join("/", this.code)), additional);
        if (!this.toCraft.isEmpty() && isUpgradedWith(AEItems.CRAFTING_CARD)){
            scheduleCrafts();
            isCrafting = true;
        } else {
            this.isCrafting = false;
            this.isRunning = true;
            this.currentInstruction = 0;
            this.tickDelayLeft = 0;
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.setChanged();
        if (inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(1).isEmpty()) {
            isRunning = false;
            code = new ArrayList<>();
            currentInstruction = 0;
            tickDelayLeft = 0;
            setGhostRenderPos(worldPosition.above().above());
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
        var grid = getGridNode().getGrid();
        var inserted = StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), what, amount, IActionSource.ofMachine(this), mode);

        checkBlocksInStorage(ProgramExpander.countUsedBlocks(String.join("/", this.code)), new GenericStack(what, amount));
        if (this.toCraft.isEmpty()){
            this.isCrafting = false;
            onRedstoneActivate(new GenericStack(what, amount));
        }
        return inserted;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (link.isCanceled() || link.isDone()) {
            this.craftingLinks.remove(link);
        }
        if (link.isCanceled()){
            var iterator = craftingLinks.iterator();
            while (iterator.hasNext()){
                iterator.next().cancel();
                iterator.remove();
            }
        }
    }
}