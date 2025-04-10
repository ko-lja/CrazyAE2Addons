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
    public boolean initialized = false;

    public DataProcessorScreen(
            DataProcessorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        this.inval = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        setupGui();
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            this.inval.setValue(getMenu().in);
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
        this.widgets.add("inval", this.inval);
        this.widgets.addButton("save", Component.literal("save"), (btn) -> {save();});
        this.widgets.addButton("clear", Component.literal("clr"), (btn) -> {clr();});
    }

    public void settings(int index){
        getMenu().openSubMenu(index);
    }

    public static boolean isAsciiNotNumber(String input) {
        return input.chars().allMatch(c -> c <= 127 && !Character.isDigit(c));
    }

    public void save(){
        if(!this.inval.getValue().isEmpty() && isAsciiNotNumber(this.inval.getValue())
                && this.inval.getValue().startsWith("&") && !this.inval.getValue().contains(" ")){
            this.inval.setTextColor(0x00FF00);
            Utils.asyncDelay(() -> this.inval.setTextColor(0xFFFFFF), 1);
            getMenu().save(this.inval.getValue().toUpperCase());
            this.inval.setValue(this.inval.getValue().toUpperCase());
        } else {
            this.inval.setTextColor(0xFF0000);
            Utils.asyncDelay(() -> this.inval.setTextColor(0xFFFFFF), 1);
        }
    }

    public void clr(){
        this.inval.setValue("");
        this.getMenu().save("");
    }
}