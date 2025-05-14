package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.parts.EntityTickerPart;
import net.oktawia.crazyae2addons.menus.EntityTickerMenu;

import static java.lang.Math.pow;

public class EntityTickerScreen<C extends EntityTickerMenu> extends UpgradeableScreen<C> {

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        double powerUsage =  CrazyConfig.COMMON.EntityTickerCost.get() * pow(4, getMenu().upgradeNum);
        setTextContent("energy", Component.literal(String.format("Energy Usage: %s FE/t", Utils.shortenNumber(powerUsage))));
        setTextContent("speed", Component.literal(String.format("Current multiplier: %d", (int) pow(2, (getMenu().upgradeNum + 1)))));
    }

    public EntityTickerScreen(
            EntityTickerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        this.setupGui();
    }

    public void setupGui(){
        this.setTextContent("info1", Component.literal("Each card multiplies"));
        this.setTextContent("info2", Component.literal("machines speed by 4"));
    }

    public void refreshGui(){
        double powerUsage = CrazyConfig.COMMON.EntityTickerCost.get() * pow(4, getMenu().upgradeNum);
        setTextContent("energy", Component.literal(String.format("Energy Usage: %s FE/t", Utils.shortenNumber(powerUsage))));
        setTextContent("speed", Component.literal(String.format("Current multiplier: %d", (int) pow(2,  (getMenu().upgradeNum + 1)))));
    }
}