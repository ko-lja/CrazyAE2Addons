package net.oktawia.crazyae2addons.misc;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RestrictedSlot extends Slot {
    public ItemStack what;
    public RestrictedSlot(Container container, int index, int x, int y, ItemStack what) {
        super(container, index, x, y);
        this.what = what;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(what.getItem());
    }
}