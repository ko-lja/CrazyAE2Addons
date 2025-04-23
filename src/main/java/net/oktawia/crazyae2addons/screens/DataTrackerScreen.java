package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DataTrackerMenu;

public class DataTrackerScreen<C extends DataTrackerMenu> extends AEBaseScreen<C> {
    public AETextField value;
    public Button confirm;
    public boolean initialized = false;

    public DataTrackerScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("value", value);
        this.widgets.add("confirm", confirm);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!this.initialized){
            value.setValue(getMenu().variable);
            this.initialized = true;
        }
    }

    private void setupGui(){
        this.setTextContent("info1", Component.literal("Enter &VARIABLE"));
        this.setTextContent("info2", Component.literal("to track its state"));
        value = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        value.setBordered(false);
        value.setMaxLength(999);
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Save"), btn -> {save();}, Minecraft.getInstance().font);
    }

    public static boolean isAsciiNotNumber(String input) {
        return input.chars().allMatch(c -> c <= 127 && !Character.isDigit(c));
    }

    public static boolean dataCheck(String input){
        if(input.isEmpty()) return true;

        return (input.startsWith("&") && !input.contains(" ") && isAsciiNotNumber(input));
    }

    private void save(){
        String input = value.getValue().toUpperCase();
        value.setValue(input);
        if (dataCheck(input)){
            value.setTextColor(0x00FF00);
            Runnable setColorFunction = () -> value.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
            getMenu().syncVariable(input);
        } else {
            value.setTextColor(0xFF0000);
            Runnable setColorFunction = () -> value.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
        }
    }
}