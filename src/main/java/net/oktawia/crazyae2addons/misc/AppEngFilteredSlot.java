package net.oktawia.crazyae2addons.misc;

import appeng.api.inventories.InternalInventory;
import appeng.core.AppEng;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.item.ItemStack;

public class AppEngFilteredSlot extends AppEngSlot {

    public static ItemStack filter;
    public AppEngFilteredSlot(InternalInventory inv, int invSlot, ItemStack filter) {
        super(inv, invSlot);
        this.filter = filter;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.isSlotEnabled() && filter.getItem() == stack.getItem()) {
            return true;
        }
        return false;
    }
}
