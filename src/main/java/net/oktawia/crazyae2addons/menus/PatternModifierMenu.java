package net.oktawia.crazyae2addons.menus;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.AutoEnchanterBE;
import net.oktawia.crazyae2addons.entities.PatternModifierBE;
import net.oktawia.crazyae2addons.screens.PatternModifierScreen;

public class PatternModifierMenu extends UpgradeableMenu<PatternModifierBE> {

    public static String SYNC_TAG = "syncTag";
    public static PatternModifierScreen<?> screen;

    public PatternModifierMenu(int id, Inventory ip, PatternModifierBE host) {
        super(Menus.PATTERN_MODIFIER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        this.addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.STORAGE);
        registerClientAction(SYNC_TAG, this::syncTag);
    }

    public void syncTag(){
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        if (getHost().isValidItem(AEItemKey.of(item))){
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
        getHost().updateText(item);
        if (isClientSide()) {
            sendClientAction(SYNC_TAG);
        }
    }

    public void setScreen(PatternModifierScreen<?> scr){
        screen = scr;
    }

    public PatternModifierScreen<?> getScreen(){
        return screen;
    }

    public void setText(String text){
        this.getScreen().setText(text);
    }
}
