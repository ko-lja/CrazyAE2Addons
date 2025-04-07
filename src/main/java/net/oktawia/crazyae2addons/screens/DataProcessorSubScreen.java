package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.menus.DataProcessorMenu;
import net.oktawia.crazyae2addons.menus.DataProcessorSubMenu;
import net.oktawia.crazyae2addons.records.LogicSetting;

import java.util.Map;

public class DataProcessorSubScreen<C extends DataProcessorSubMenu> extends UpgradeableScreen<C> {
    public AETextField in1;
    public AETextField in2;
    public AETextField out;
    public boolean initialized = false;
    public DataProcessorSubScreen(
            DataProcessorSubMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        in1 = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        in2 = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        out = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        setupGui();
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            Map<Integer, LogicSetting> settings = DataProcessorBE.loadSettings(getMenu().cardSettings);
            in1.setValue(settings.get(getMenu().submenuNum).in1);
            in2.setValue(settings.get(getMenu().submenuNum).in2);
            out.setValue(settings.get(getMenu().submenuNum).out);
            this.initialized = true;
        }
    }

    public void setupGui(){
        this.widgets.addButton("back", Component.literal("<-"), (btn) -> {getMenu().closeSubScreen();});
        this.widgets.addButton("save", Component.literal("save"), (btn) -> {saveData();});
        in1.setBordered(false);
        in1.setMaxLength(999);
        in1.setPlaceholder(Component.literal("Value IN"));
        in2.setBordered(false);
        in2.setMaxLength(999);
        in2.setPlaceholder(Component.literal("Value IN"));
        out.setBordered(false);
        out.setMaxLength(999);
        out.setPlaceholder(Component.literal("Value OUT"));
        if(getMenu().submenuNum == 0){
            in1.setEditable(false);
        }
        this.widgets.add("in1", in1);
        this.widgets.add("in2", in2);
        this.widgets.add("out", out);
    }

    public static boolean dataCheck(String input){
        if(input.isEmpty()) return false;

        if (input.matches("-?\\d+(\\.\\d+)?")) return true;

        if (input.startsWith("&") && !input.contains(" ")) return true;

        return input.equals("&&0") || input.equals("&&1") || input.equals("&&2") || input.equals("&&3");
    }

    public void saveData(){
        if (dataCheck(in1.getValue()) && dataCheck(in2.getValue()) && dataCheck(out.getValue())){
            in1.setTextColor(0x00FF00);
            in2.setTextColor(0x00FF00);
            out.setTextColor(0x00FF00);
            Utils.asyncDelay(() -> in1.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> in2.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> out.setTextColor(0xFFFFFF), 1);
            getMenu().setSetting(in1.getValue().toUpperCase(), in2.getValue().toUpperCase(), out.getValue().toUpperCase());
        } else {
            in1.setTextColor(0xFF0000);
            in2.setTextColor(0xFF0000);
            out.setTextColor(0xFF0000);
            Utils.asyncDelay(() -> in1.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> in2.setTextColor(0xFFFFFF), 1);
            Utils.asyncDelay(() -> out.setTextColor(0xFFFFFF), 1);
        }
    }
}