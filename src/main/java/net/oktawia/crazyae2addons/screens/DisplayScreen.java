package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DisplayMenu;
import net.oktawia.crazyae2addons.menus.EntityTickerMenu;
import net.oktawia.crazyae2addons.menus.NBTExportBusMenu;

public class DisplayScreen<C extends DisplayMenu> extends AEBaseScreen<C> {

    public AETextField value;
    public Button confirm;
    public boolean initialized = false;

    public DisplayScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("value", value);
        this.widgets.add("confirm", confirm);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!this.initialized){
            value.setValue(getMenu().displayValue);
            this.initialized = true;
        }
    }

    private void setupGui(){
        this.setTextContent("info1", Component.literal("Use \"&nl\" for new lines"));
        this.setTextContent("info2", Component.literal("or \"&NAME for variables"));
        value = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        value.setBordered(false);
        value.setMaxLength(9999);
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Save"), btn -> {save();}, Minecraft.getInstance().font);
    }

    private void save(){
        String input = value.getValue();
        value.setTextColor(0x00FF00);
        Runnable setColorFunction = () -> value.setTextColor(0xFFFFFF);
        Utils.asyncDelay(setColorFunction, 1);
        getMenu().syncValue(input);
    }
}