package net.oktawia.crazyae2addons.menus;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;

public class CrazyPatternProviderMenu extends PatternProviderMenu {
    public CrazyPatternProviderMenu(int id, Inventory ip, PatternProviderLogicHost host) {
        super(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), id, ip, host);
        setupSlots();
    }

    private void setupSlots() {
        getSlots(SlotSemantics.STORAGE).forEach(slot -> {
            if (slot instanceof AppEngSlot slt){
                slt.setSlotEnabled(false);
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack copy = sourceStack.copy();

        int playerInvStart = 0;
        int playerInvEnd = 36;
        int patternStart = 36;
        int patternEnd = 36 + 81;

        if (index >= playerInvStart && index < playerInvEnd) {
            for (Slot target : this.getSlots(SlotSemantics.ENCODED_PATTERN)) {
                if (target.isActive() && target.mayPlace(sourceStack)) {
                    if (this.moveItemStackTo(sourceStack, target.index, target.index + 1, false)) {
                        break;
                    }
                }
            }
        } else if (index >= patternStart && index < patternEnd) {
            if (!this.moveItemStackTo(sourceStack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }
}