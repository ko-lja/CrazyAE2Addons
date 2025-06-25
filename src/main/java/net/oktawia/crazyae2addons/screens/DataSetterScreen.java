package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DataSetterMenu;

public class DataSetterScreen<C extends DataSetterMenu> extends AEBaseScreen<C> {

    public AETextField value;
    public AETextField variable;
    public Button confirm;
    public boolean initialized = false;

    public DataSetterScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("value", value);
        this.widgets.add("variable", variable);
        this.widgets.add("confirm", confirm);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!this.initialized){
            value.setValue(String.valueOf(getMenu().valueToSet));
            variable.setValue(getMenu().variableToSet);
            this.initialized = true;
        }
    }

    private void setupGui(){
        this.setTextContent("info1", Component.literal("Use &NAME for variables"));
        value = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        value.setBordered(false);
        value.setPlaceholder(Component.literal("Value"));
        value.setMaxLength(10);
        variable = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        variable.setBordered(false);
        variable.setMaxLength(32);
        variable.setPlaceholder(Component.literal("Name"));
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Save"), btn -> {save();}, Minecraft.getInstance().font);
    }

    private void save(){
        String inputVal = value.getValue();
        String inputName = variable.getValue();
        boolean parsable = false;
        try {
            Integer.parseInt(inputVal);
            parsable = true;
        } catch (NumberFormatException ignored) {}
        if (!parsable) return;
        value.setTextColor(0x00FF00);
        Runnable setColorFunction = () -> value.setTextColor(0xFFFFFF);
        Utils.asyncDelay(setColorFunction, 1);
        variable.setTextColor(0x00FF00);
        Runnable setColorFunction2 = () -> variable.setTextColor(0xFFFFFF);
        Utils.asyncDelay(setColorFunction2, 1);
        getMenu().syncVariable(inputName);
        getMenu().syncValue(Integer.parseInt(inputVal));
    }
}