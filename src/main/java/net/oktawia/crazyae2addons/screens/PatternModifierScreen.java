package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.PatternModifierMenu;

public class PatternModifierScreen<C extends PatternModifierMenu> extends UpgradeableScreen<C> {
    private static PlainTextButton confirm;
    private static PlainTextButton status;
    private String text = "No Item";

    public PatternModifierScreen(PatternModifierMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        menu.setScreen(this);
        setupGui();
        this.widgets.add("confirm", confirm);
    }
    private void setupGui(){
        confirm = new PlainTextButton(
                0,0,0,0, Component.literal("Modify"), btn -> {modifyPattern();}, Minecraft.getInstance().font);
    }

    private void modifyPattern() {
        this.getMenu().syncTag();
    }

    public void setText(String text){
        this.text = text;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, this.getGuiLeft() + 88, this.getGuiTop() + 30, 0xFFFFFF);
    }
}
