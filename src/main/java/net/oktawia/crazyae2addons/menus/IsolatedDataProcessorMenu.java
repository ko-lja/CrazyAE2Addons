package net.oktawia.crazyae2addons.menus;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.locator.MenuLocators;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.entities.IsolatedDataProcessorBE;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;

import java.util.List;

public class IsolatedDataProcessorMenu extends UpgradeableMenu<IsolatedDataProcessorBE> implements IUpgradeableObject {

    public final String OPEN_SUB = "actionOpenSubmenu";

    public IsolatedDataProcessorMenu(int id, Inventory ip, IsolatedDataProcessorBE host) {
        super(Menus.ISOLATED_DATA_PROCESSOR_MENU, id, ip, host);
        getHost().setMenu(this);
        List<ItemStack> allowedItems = Items.getCards().stream().map(ItemDefinition::stack).toList();
        for (int i = 0; i < getHost().getInternalInventory().size(); i ++){
            this.addSlot(new AppEngManyFilteredSlot(getHost().inv, i, allowedItems), SlotSemantics.STORAGE);
        }
        registerClientAction(OPEN_SUB, Integer.class, this::openSubMenu);
    }

    public void openSubMenu(Integer index) {
        getHost().submenuNum = index;
        if(isClientSide()){
            sendClientAction(OPEN_SUB, index);
        } else {
            MenuOpener.open(Menus.ISOLATED_DATA_PROCESSOR_SUBMENU, getPlayer(), MenuLocators.forBlockEntity(getBlockEntity()), true);
        }
    }
}
