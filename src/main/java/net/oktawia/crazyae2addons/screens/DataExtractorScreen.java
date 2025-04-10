package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import java.util.Arrays;

public class DataExtractorScreen<C extends DataExtractorMenu> extends UpgradeableScreen<C> {

    public boolean initialized = false;
    public boolean initialized2 = false;
    public AbstractWidget btn0;
    public AbstractWidget btn1;
    public AbstractWidget btn2;
    public AbstractWidget btn3;
    public AbstractWidget btn4;
    public AETextField input;
    public AETextField delay;

    public DataExtractorScreen(
            DataExtractorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        getMenu().screen = this;
        if (!this.initialized){
            setupGui();
            this.initialized = true;
        }
        renderPage(getMenu().page * 4, (getMenu().page + 1) * 4);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized2){
            this.input.setValue(this.getMenu().valueName);
            this.delay.setValue(String.valueOf(this.getMenu().delay));
            initialized2 = true;
        }
        String selected;
        if (!getMenu().available.isEmpty()){
            selected = "Selected: " + Arrays.stream(getMenu().available.split("\\|")).toList().get(getMenu().selected);
        } else {
            selected = "Selected: ";
        }
        setTextContent("selectedValue", Component.literal(selected));
    }

    public void setupGui(){
        btn0 = Button.builder(Component.literal("0 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn1 = Button.builder(Component.literal("1 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn2 = Button.builder(Component.literal("2 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn3 = Button.builder(Component.literal("3 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn4 = Button.builder(Component.literal("4 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        this.widgets.add("button0", btn0);
        this.widgets.add("button1", btn1);
        this.widgets.add("button2", btn2);
        this.widgets.add("button3", btn3);
        this.widgets.addButton("data", Component.literal("fetch"), (btn) -> {getMenu().getData(); updateGui();});
        this.widgets.addButton("down", Component.literal("<"), (btn) -> {getMenu().page --; updateGui();});
        this.widgets.addButton("up", Component.literal(">"), (btn) -> {getMenu().page ++; updateGui();});
        this.widgets.addButton("save", Component.literal("+"), (btn) -> {updateVariableName();});
        this.input = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.input.setPlaceholder(Component.literal("Variables Name"));
        this.input.setValue(getMenu().valueName);
        this.input.setMaxLength(999);
        this.input.setBordered(false);
        this.delay = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.delay.setPlaceholder(Component.literal("Delay"));
        this.delay.setValue(String.valueOf(getMenu().delay));
        this.delay.setMaxLength(10);
        this.delay.setBordered(false);
        this.widgets.add("input", this.input);
        this.widgets.add("delay", this.delay);
    }

    public void renderPage(int start, int end){
        setTextContent("selectedValue", Component.literal("Selected: " + Arrays.stream(getMenu().available.split("\\|")).toList().get(getMenu().selected)));
        int theI;
        for (int i = start; i < end; i ++){
            if (i >= Arrays.stream(getMenu().available.split("\\|")).toList().size() || i < 0){
                theI = -1;
            } else {
                theI = i;
            }
            String[] parts = getMenu().available.split("\\|");
            String value = (i >= 0 && i < parts.length) ? parts[i] : "???";
            switch (i % 4){
                case 0 -> {
                    btn0.setMessage(Component.literal(theI + " " + value));
                }
                case 1 -> {
                    btn1.setMessage(Component.literal(theI + " " + value));
                }
                case 2 -> {
                    btn2.setMessage(Component.literal(theI + " " + value));
                }
                case 3 -> {
                    btn3.setMessage(Component.literal(theI + " " + value));
                }
                case 4 -> {
                    btn4.setMessage(Component.literal(theI + " " + value));
                }
            }
        }
    }

    public void setSelected(Integer what){
        if (what == -1){
            return;
        }
        getMenu().selected = what;
        getMenu().syncValue(what);
        updateGui();
    }

    public void updateGui(){
        setTextContent("selectedValue", Component.literal("Selected: " + Arrays.stream(getMenu().available.split("\\|")).toList().get(getMenu().selected)));
        renderPage(getMenu().page * 4, (getMenu().page + 1) * 4);
    }

    public static boolean isAscii(String input) {
        return input.chars().allMatch(c -> c <= 127);
    }

    public void updateVariableName(){
        String name = this.input.getValue();
        String delay = this.delay.getValue();
        if (isAscii(name) && !name.isEmpty() && delay.chars().allMatch(Character::isDigit)){
            name = name.toUpperCase();
            this.input.setTextColor(0x00FF00);
            Runnable setColorFunction = () -> this.input.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
            getMenu().saveName(name);
            getMenu().saveDelay(Integer.parseInt(delay));
        }
    }

}