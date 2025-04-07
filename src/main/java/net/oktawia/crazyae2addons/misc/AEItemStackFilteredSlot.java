package net.oktawia.crazyae2addons.misc;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class AEItemStackFilteredSlot implements IAEItemFilter {

    public ItemStack item;

    public AEItemStackFilteredSlot(ItemStack input) {
        this.item = input;
    }

    @Override
    public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
        return this.item.equals(stack);
    }

}