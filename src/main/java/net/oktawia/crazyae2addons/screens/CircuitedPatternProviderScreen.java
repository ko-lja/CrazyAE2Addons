package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.CircuitedPatternProviderMenu;

public class CircuitedPatternProviderScreen<C extends CircuitedPatternProviderMenu> extends PatternProviderScreen<C> {

    public CircuitedPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}
