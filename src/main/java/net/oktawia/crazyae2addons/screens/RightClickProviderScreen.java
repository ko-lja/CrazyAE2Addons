package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.RightClickProviderMenu;

public class RightClickProviderScreen<C extends RightClickProviderMenu> extends UpgradeableScreen<C> {
    public RightClickProviderScreen(RightClickProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
    }
}
