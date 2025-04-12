package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.EnergyExporterMenu;
import net.oktawia.crazyae2addons.menus.PatternModifierMenu;

public class EnergyExporterScreen<C extends EnergyExporterMenu> extends UpgradeableScreen<C> {
    public EnergyExporterScreen(EnergyExporterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        setTextContent("fe",Component.literal(String.format("Output: %s FE/t", getMenu().transfered)));
        if (getMenu().greg){
            setTextContent("volt",Component.literal(String.format("Voltage: %s", getMenu().voltage)));
            setTextContent("amp",Component.literal(String.format("Amperage: %s", getMenu().maxAmps)));
        } else {
            setTextContent("volt",Component.empty());
            setTextContent("amp",Component.empty());
        }
    }
}
