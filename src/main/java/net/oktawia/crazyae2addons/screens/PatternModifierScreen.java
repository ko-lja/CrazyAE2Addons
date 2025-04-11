package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.PatternModifierMenu;

public class PatternModifierScreen<C extends PatternModifierMenu> extends UpgradeableScreen<C> {
    private static PlainTextButton confirm;
    public AETextField value;
    public Button confirmCirc;

    public PatternModifierScreen(PatternModifierMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("confirm", confirm);
        this.widgets.add("value", value);
        this.widgets.add("confirmcirc", confirmCirc);
    }
    private void setupGui(){
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Modify"), btn -> {modifyPattern();}, Minecraft.getInstance().font);
        value = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        value.setBordered(false);
        value.setMaxLength(2);
        confirmCirc = new PlainTextButton(
                0,0,0,0, Component.literal("Save"), btn -> {modifyCirc();}, Minecraft.getInstance().font);
    }

    private void modifyPattern() {
        this.getMenu().changeIgnoreNBT();
    }

    private void modifyCirc(){
        if (value.getValue().chars().allMatch(Character::isDigit)){
            this.getMenu().changeCircuit(Integer.parseInt(value.getValue()));
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMenu().textNBT, this.getGuiLeft() + 88, this.getGuiTop() + 30, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMenu().textCirc, this.getGuiLeft() + 88, this.getGuiTop() + 60, 0xFFFFFF);
    }
}
