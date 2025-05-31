package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DataProcessorMenu;
import net.oktawia.crazyae2addons.menus.EjectorMenu;

public class EjectorScreen<C extends EjectorMenu> extends UpgradeableScreen<C> {
    public EjectorScreen(
            EjectorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
    }
}