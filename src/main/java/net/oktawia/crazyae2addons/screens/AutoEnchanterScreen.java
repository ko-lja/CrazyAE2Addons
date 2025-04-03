package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.AutoEnchanterMenu;

public class AutoEnchanterScreen<C extends AutoEnchanterMenu> extends UpgradeableScreen<C> {

    public AutoEnchanterScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.widgets.addButton("levelone", Component.literal("Enchant Level 1"), () -> {this.getMenu().syncLevel(1);});
        this.widgets.addButton("leveltwo", Component.literal("Enchant Level 2"), () -> {this.getMenu().syncLevel(2);});
        this.widgets.addButton("levelthree", Component.literal("Enchant Level 3"), () -> {this.getMenu().syncLevel(3);});
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "%.2f%%".formatted((float) getMenu().holdedXp / getMenu().xpCap * 100), this.getGuiLeft() + 88, this.getGuiTop() + 30, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "XP Amount", this.getGuiLeft() + 88, this.getGuiTop() + 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "Selected Level: %s".formatted(getMenu().selectedLevel), this.getGuiLeft() + 115, this.getGuiTop() + 45, 0xFFFFFF);
    }
}
