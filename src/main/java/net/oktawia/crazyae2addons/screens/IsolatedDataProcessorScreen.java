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
import net.oktawia.crazyae2addons.menus.IsolatedDataProcessorMenu;

public class IsolatedDataProcessorScreen<C extends IsolatedDataProcessorMenu> extends UpgradeableScreen<C> {

    public IsolatedDataProcessorScreen(
            IsolatedDataProcessorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        setupGui();
    }

    public void setupGui(){
        this.widgets.addButton("button0", Component.literal("⚙"), (btn) -> {settings(0);});
        this.widgets.addButton("button1", Component.literal("⚙"), (btn) -> {settings(1);});
        this.widgets.addButton("button2", Component.literal("⚙"), (btn) -> {settings(2);});
        this.widgets.addButton("button3", Component.literal("⚙"), (btn) -> {settings(3);});
        this.widgets.addButton("button4", Component.literal("⚙"), (btn) -> {settings(4);});
        this.widgets.addButton("button5", Component.literal("⚙"), (btn) -> {settings(5);});
        this.widgets.addButton("button6", Component.literal("⚙"), (btn) -> {settings(6);});
        this.widgets.addButton("button7", Component.literal("⚙"), (btn) -> {settings(7);});
        this.widgets.addButton("button8", Component.literal("⚙"), (btn) -> {settings(8);});
    }

    public void settings(int index){
        getMenu().openSubMenu(index);
    }
}