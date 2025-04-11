package net.oktawia.crazyae2addons.entities;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.util.SettingsFrom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.logic.CircuitedPatternProviderLogic;
import net.oktawia.crazyae2addons.logic.CircuitedPatternProviderLogicHost;
import net.oktawia.crazyae2addons.menus.CraftingCancelerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CircuitedPatternProviderBE extends PatternProviderBlockEntity implements CircuitedPatternProviderLogicHost {
    protected final CircuitedPatternProviderLogic logic = createLogic();

    @Nullable
    private PushDirection pendingPushDirectionChange;

    public CircuitedPatternProviderBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    protected CircuitedPatternProviderLogic createLogic() {
        return new CircuitedPatternProviderLogic(this.getMainNode(), this, 36);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    private PushDirection getPushDirection() {
        return getBlockState().getValue(PatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        // In omnidirectional mode, every side is grid-connectable
        var pushDirection = getPushDirection().getDirection();
        if (pushDirection == null) {
            return EnumSet.allOf(Direction.class);
        }

        // Otherwise all sides *except* the target side are connectable
        return EnumSet.complementOf(EnumSet.of(pushDirection));
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        this.logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.clearContent();
    }

    @Override
    public void onReady() {
        if (pendingPushDirectionChange != null) {
            level.setBlockAndUpdate(
                    getBlockPos(),
                    getBlockState().setValue(PatternProviderBlock.PUSH_DIRECTION, pendingPushDirectionChange));
            pendingPushDirectionChange = null;
            onGridConnectableSidesChanged();
        }

        super.onReady();
        this.logic.updatePatterns();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.logic.writeToNBT(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        if (data.getBoolean("omniDirectional")) {
            pendingPushDirectionChange = PushDirection.ALL;
        } else if (data.contains("forward", Tag.TAG_STRING)) {
            try {
                var forward = Direction.valueOf(data.getString("forward").toUpperCase(Locale.ROOT));
                pendingPushDirectionChange = PushDirection.fromDirection(forward);
            } catch (IllegalArgumentException ignored) {
            }
        }

        this.logic.readFromNBT(data);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public CircuitedPatternProviderLogic getLogic() {
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        var pushDirection = getPushDirection();
        if (pushDirection.getDirection() == null) {
            return EnumSet.allOf(Direction.class);
        } else {
            return EnumSet.of(pushDirection.getDirection());
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return super.getConfigManager();
    }

    @Override
    public int getPriority() {
        return super.getPriority();
    }

    @Override
    public void setPriority(int newValue) {
        super.setPriority(newValue);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.CIRCUITED_PATTERN_PROVIDER_MENU, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(Menus.CIRCUITED_PATTERN_PROVIDER_MENU, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public @Nullable IGrid getGrid() {
        return super.getGrid();
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(Blocks.CIRCUITED_PATTERN_PROVIDER_BLOCK.asItem());
    }

    @Override
    public boolean isVisibleInTerminal() {
        return super.isVisibleInTerminal();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return super.getTerminalPatternInventory();
    }

    @Override
    public long getTerminalSortOrder() {
        return super.getTerminalSortOrder();
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return super.getTerminalGroup();
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output,
                               @org.jetbrains.annotations.Nullable Player player) {
        super.exportSettings(mode, output, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(output);

            var pushDirection = getPushDirection();
            output.putByte("push_direction", (byte) pushDirection.ordinal());
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input,
                               @org.jetbrains.annotations.Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);

            // Restore push direction blockstate
            if (input.contains(PatternProviderBlock.PUSH_DIRECTION.getName(), Tag.TAG_BYTE)) {
                var pushDirection = input.getByte(PatternProviderBlock.PUSH_DIRECTION.getName());
                if (pushDirection >= 0 && pushDirection < PushDirection.values().length) {
                    var level = getLevel();
                    if (level != null) {
                        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(
                                PatternProviderBlock.PUSH_DIRECTION,
                                PushDirection.values()[pushDirection]));
                    }
                }
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @javax.annotation.Nullable Direction side) {
        var lo = logic.getCapability(cap);
        if (lo.isPresent()) {
            return lo;
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return Blocks.CIRCUITED_PATTERN_PROVIDER_BLOCK.stack();
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        onGridConnectableSidesChanged();
    }
}