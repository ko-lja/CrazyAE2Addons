package net.oktawia.crazyae2addons.entities;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.PatternModifierMenu;
import org.jetbrains.annotations.Nullable;

public class PatternModifierBE extends AEBaseInvBlockEntity implements MenuProvider, IUpgradeableObject {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    public ConfigInventory config;
    public PatternModifierMenu menu;

    public PatternModifierBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.config = ConfigInventory.configTypes(1, null);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        getMenu().updateText(inv.getStackInSlot(slot));
    }

    public void setMenu(PatternModifierMenu menu){
        this.menu = menu;
    }

    public PatternModifierMenu getMenu(){
        return this.menu;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player pPlayer) {
        return new PatternModifierMenu(i, inventory, this);
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.PATTERN_MODIFIER_MENU, player, locator);
    }
}
