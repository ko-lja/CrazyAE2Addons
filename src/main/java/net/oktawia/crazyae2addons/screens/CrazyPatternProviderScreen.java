package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.oktawia.crazyae2addons.menus.CrazyPatternProviderMenu;
import net.oktawia.crazyae2addons.mixins.SlotAccessor;

public class CrazyPatternProviderScreen<C extends CrazyPatternProviderMenu> extends PatternProviderScreen<C> {
    private Scrollbar scrollbar;

    public CrazyPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        scrollbar = new Scrollbar();
        scrollbar.setRange(0, 5, 1);
        this.widgets.add("scrollbar", scrollbar);
    }

    public void updateBeforeRender() {
        super.updateBeforeRender();

        int scrollOffset = this.scrollbar.getCurrentScroll();

        for (int i = 0; i < 81; i++) {
            int row = i / 9;
            int col = i % 9;

            int x = 8 + col * 18;
            int y = 42 + (row - scrollOffset) * 18;

            Slot s = this.menu.getSlots(appeng.menu.SlotSemantics.ENCODED_PATTERN).get(i);
            if (!(s instanceof AppEngSlot slot)) return;
            SlotAccessor accessor = (SlotAccessor) slot;

            if (row >= scrollOffset && row < scrollOffset + 4) {
                accessor.setX(x);
                accessor.setY(y);
                slot.setSlotEnabled(true);
            } else {
                slot.setSlotEnabled(false);
            }
        }
    }
}