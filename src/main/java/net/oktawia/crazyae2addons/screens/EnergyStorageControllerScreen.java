package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.EnergyStorageControllerMenu;
import net.oktawia.crazyae2addons.menus.EntityTickerMenu;
import net.oktawia.crazyae2addons.misc.IconButton;

import static java.lang.Math.pow;

public class EnergyStorageControllerScreen<C extends EnergyStorageControllerMenu> extends AEBaseScreen<C> {

    public EnergyStorageControllerScreen(
            C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        var prevBtn = new IconButton(Icon.ENTER, btn -> getMenu().changePreview(!getMenu().preview));
        prevBtn.setTooltip(Tooltip.create(Component.literal("Enable/Disable preview")));
        this.widgets.add("prevbtn", prevBtn);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        setTextContent("energy", Component.literal(String.format("Storing: %s/%s AE", Utils.shortenNumber(getMenu().energy), Utils.shortenNumber(getMenu().maxEnergy))));
        setTextContent("prev", Component.literal("Preview: " + getMenu().preview));
    }
}