package net.oktawia.crazyae2addons.menus;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.PatternModifierBE;
import net.oktawia.crazyae2addons.screens.PatternModifierScreen;

public class PatternModifierMenu extends UpgradeableMenu<PatternModifierBE> {

    public static String SYNC_TAG = "syncTag";
    public static PatternModifierScreen<?> screen;

    @GuiSync(8921)
    public String text = "No Item";

    public PatternModifierMenu(int id, Inventory ip, PatternModifierBE host) {
        super(Menus.PATTERN_MODIFIER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        this.addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.STORAGE);
        registerClientAction(SYNC_TAG, this::syncTag);
    }

    public void syncTag(){
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        if (isValidItem(AEItemKey.of(item))){
            CompoundTag currentTag = item.getOrCreateTag();
            boolean tag;
            if (currentTag.contains("ignorenbt")){
                tag = !currentTag.getBoolean("ignorenbt");
            } else {
                tag = true;
            }
            currentTag.putBoolean("ignorenbt", tag);
            item.setTag(currentTag);
        }
        updateText(item);
        if (isClientSide()) {
            sendClientAction(SYNC_TAG);
        }
    }

    public void updateText(ItemStack stack){
        if (stack.isEmpty()){
            text = "No Item";
            return;
        }
        AEItemKey item = AEItemKey.of(stack);
        if (isValidItem(item)){
            CompoundTag currentTag = item.getTag();
            boolean nbtIgnoreValue = currentTag.getBoolean("ignorenbt");
            if (nbtIgnoreValue){
                text = "Setting: Ignore NBT";
            } else {
                text = "Setting: DO NOT Ignore NBT";
            }
        } else {
            text = "Invalid Item";
        }
    }

    public boolean isValidItem(AEItemKey item) {
        return item.fuzzyEquals(AEItemKey.of(AEItems.PROCESSING_PATTERN.stack()), FuzzyMode.IGNORE_ALL);
    }
}
