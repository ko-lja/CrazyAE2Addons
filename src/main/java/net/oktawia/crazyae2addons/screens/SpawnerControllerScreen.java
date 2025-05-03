package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.SpawnerControllerMenu;

public class SpawnerControllerScreen<C extends SpawnerControllerMenu> extends UpgradeableScreen<C> {
    public SpawnerControllerScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
    }
}
