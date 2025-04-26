package net.oktawia.crazyae2addons.menus;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.locator.MenuLocators;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.IsolatedDataProcessorBE;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;

import java.util.List;

public class IsolatedDataProcessorMenu extends UpgradeableMenu<IsolatedDataProcessorBE> implements IUpgradeableObject {

    public final String OPEN_SUB = "actionOpenSubmenu";

    public IsolatedDataProcessorMenu(int id, Inventory ip, IsolatedDataProcessorBE host) {
        super(CrazyMenuRegistrar.ISOLATED_DATA_PROCESSOR_MENU.get(), id, ip, host);
        getHost().setMenu(this);
        List<ItemStack> allowedItems = CrazyItemRegistrar.getCards().stream().map(x -> x.asItem().getDefaultInstance()).toList();
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
            MenuOpener.open(CrazyMenuRegistrar.ISOLATED_DATA_PROCESSOR_SUB_MENU.get(), getPlayer(), MenuLocators.forBlockEntity(getBlockEntity()), true);
        }
    }
}
