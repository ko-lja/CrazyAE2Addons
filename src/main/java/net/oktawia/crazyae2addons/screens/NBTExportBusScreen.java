package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.TagParser;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.NBTExportBusMenu;

public class NBTExportBusScreen<C extends NBTExportBusMenu> extends AEBaseScreen<C> {
    private static AETextField data;
    private static PlainTextButton confirm;
    private static AECheckbox matchMode;
    public static boolean initialized;

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            data.setValue(String.valueOf(getMenu().data));
            matchMode.setSelected(getMenu().mode);
            initialized = true;
        }
    }

    public NBTExportBusScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("data", data);
        this.widgets.add("confirm", confirm);
        this.widgets.add("mode", matchMode);
        initialized = false;
    }

    private void setupGui(){
        matchMode = new AECheckbox(0,0,0,0,style, Component.literal("Match Any"));
        data = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        data.setBordered(false);
        data.setMaxLength(9999);
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Save"), btn -> {save();}, Minecraft.getInstance().font);
    }

    public static boolean isValidNBT(String input) {
        if (input.isEmpty()){
            return true;
        }
        try {
            CompoundTag tag = TagParser.parseTag(input);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private void save(){
        String input = data.getValue();
        if (!isValidNBT(input)){
            data.setTextColor(0xFF0000);
            Runnable setColorFunction = () -> data.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
        } else {
            data.setTextColor(0x00FF00);
            Runnable setColorFunction = () -> data.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
            getMenu().updateData(input);
            getMenu().updateMatchMode(matchMode.isSelected());
        }
    }
}