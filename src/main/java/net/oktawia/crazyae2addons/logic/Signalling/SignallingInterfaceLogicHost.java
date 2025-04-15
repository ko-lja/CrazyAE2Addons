package net.oktawia.crazyae2addons.logic.Signalling;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.defs.Menus;


public interface SignallingInterfaceLogicHost extends IConfigurableObject, IUpgradeableObject, IPriorityHost, IConfigInvHost {

    BlockEntity getBlockEntity();

    void saveChanges();

    SignallingInterfaceLogic getInterfaceLogic();

    @Override
    default IConfigManager getConfigManager() {
        return getInterfaceLogic().getConfigManager();
    }

    @Override
    default IUpgradeInventory getUpgrades() {
        return getInterfaceLogic().getUpgrades();
    }

    @Override
    default int getPriority() {
        return getInterfaceLogic().getPriority();
    }

    @Override
    default void setPriority(int newValue) {
        getInterfaceLogic().setPriority(newValue);
    }

    @Override
    default GenericStackInv getConfig() {
        return getInterfaceLogic().getConfig();
    }

    default GenericStackInv getStorage() {
        return getInterfaceLogic().getStorage();
    }

    default void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.SIGNALLING_INTERFACE_MENU, player, locator);
    }

    @Override
    default void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(Menus.SIGNALLING_INTERFACE_MENU, player, subMenu.getLocator());
    }

}
