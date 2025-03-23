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
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
        updateText(inv.getStackInSlot(slot));
    }

    public boolean isValidItem(AEItemKey item){
        return item.fuzzyEquals(AEItemKey.of(AEItems.PROCESSING_PATTERN.stack()), FuzzyMode.IGNORE_ALL);
    }

    public void updateText(ItemStack stack){
        if (stack.isEmpty()){
            this.getMenu().setText("No Item");
            return;
        }
        AEItemKey item = AEItemKey.of(stack);
        if (isValidItem(item)){
            CompoundTag currentTag = item.getTag();
            boolean nbtIgnoreValue = currentTag.getBoolean("ignorenbt");
            if (nbtIgnoreValue){
                this.getMenu().setText("Setting: Ignore NBT");
            } else {
                this.getMenu().setText("Setting: DO NOT Ignore NBT");
            }
        } else {
            this.getMenu().setText("Invalid Item");
        }
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
