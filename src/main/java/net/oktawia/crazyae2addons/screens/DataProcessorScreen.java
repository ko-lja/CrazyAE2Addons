package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import net.oktawia.crazyae2addons.menus.DataProcessorMenu;

import java.util.Arrays;

public class DataProcessorScreen<C extends DataProcessorMenu> extends UpgradeableScreen<C> {
    public AETextField inval;
    public AETextField outval;
    public boolean initialized = false;

    public DataProcessorScreen(
            DataProcessorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        this.inval = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.outval = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        setupGui();
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            this.inval.setValue(getMenu().in);
            this.outval.setValue(getMenu().out);
            this.initialized = true;
        }
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
        this.inval.setBordered(false);
        this.inval.setMaxLength(999);
        this.inval.setPlaceholder(Component.literal("VARIABLE IN"));
        this.outval.setBordered(false);
        this.outval.setMaxLength(999);
        this.outval.setPlaceholder(Component.literal("VARIABLE OUT"));
        this.widgets.add("inval", this.inval);
        this.widgets.add("outval", this.outval);
        this.widgets.addButton("save", Component.literal("save"), (btn) -> {save();});
    }

    public void settings(int index){
        getMenu().openSubMenu(index);
    }

    public static boolean isAsciiNotNumber(String input) {
        return input.chars().allMatch(c -> c <= 127 && !Character.isDigit(c));
    }

    public void save(){
        if(!this.inval.getValue().isEmpty() && !this.outval.getValue().isEmpty()
                && isAsciiNotNumber(this.inval.getValue()) && isAsciiNotNumber(this.outval.getValue())
                && !this.inval.getValue().equals(this.outval.getValue())){
            this.inval.setTextColor(0x00FF00);
            this.outval.setTextColor(0x00FF00);
            Utils.asyncDelay(() -> this.inval.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> this.outval.setTextColor(0xFFFFFF), 1);
            getMenu().save(this.inval.getValue().replace("&", "") + "|" + this.outval.getValue().replace("&", ""));
        } else {
            this.inval.setTextColor(0xFF0000);
            this.outval.setTextColor(0xFF0000);
            Utils.asyncDelay(() -> this.inval.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> this.outval.setTextColor(0xFFFFFF), 1);
        }
    }
}