package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.menu.SlotSemantics;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.menus.DataProcessorSubMenu;
import net.oktawia.crazyae2addons.misc.LogicSetting;
import net.oktawia.crazyae2addons.misc.NBTContainer;
import org.jline.utils.Log;

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
            NBTContainer settings = NBTContainer.deserializeFromString(getMenu().cardSettings, getMenu().COMPRESSED);
            in1.setValue(((LogicSetting)settings.get(String.valueOf(getMenu().submenuNum))).in1);
            in2.setValue(((LogicSetting)settings.get(String.valueOf(getMenu().submenuNum))).in2);
            out.setValue(((LogicSetting)settings.get(String.valueOf(getMenu().submenuNum))).out);
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
            in1.setPlaceholder(Component.literal(getMenu().valueIn));
            in1.setEditable(false);
            in1.active = false;
        }
        ItemStack itemStack = getMenu().getSlots(SlotSemantics.STORAGE).get(0).getItem();
        if (itemStack.isEmpty()){
            in1.setEditable(false);
            in1.active = false;
            in2.setEditable(false);
            in2.active = false;
            out.setEditable(false);
            out.active = false;
        } else if(itemStack.is(Items.HIT_CARD.asItem()) || itemStack.is(Items.HIF_CARD.asItem())){
            in1.setPlaceholder(Component.literal("Value"));
            in2.setPlaceholder(Component.literal("Target"));
            out.setPlaceholder(Component.literal(""));
            out.setEditable(false);
            out.active = false;
        }
        this.widgets.add("in1", in1);
        this.widgets.add("in2", in2);
        this.widgets.add("out", out);
        this.widgets.addButton("clear", Component.literal("clr"), (btn) -> {clr();});
    }

    public static boolean dataCheck(String input){
        if(input.isEmpty()) return false;

        if(input.startsWith("&&")){
            return input.equals("&&0") || input.equals("&&1") || input.equals("&&2") || input.equals("&&3");
        }

        return (input.startsWith("&") || input.chars().allMatch(Character::isDigit)) && !input.contains(" ");
    }

    public void clr(){
        in1.setValue("");
        in2.setValue("");
        out.setValue("");
        getMenu().setSetting("", "", "");
    }

    public void saveData(){
        in1.setValue(in1.getValue().toUpperCase().strip());
        in2.setValue(in2.getValue().toUpperCase().strip());
        out.setValue(out.getValue().toUpperCase().strip());
        if ((((dataCheck(in1.getValue()) || !in1.isActive()) && dataCheck(in2.getValue()) && dataCheck(out.getValue()))
                || (!in1.isActive() && !in2.isActive() && !out.isActive())
                || (!out.isActive())) && !out.getValue().equals(getMenu().valueIn)){
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