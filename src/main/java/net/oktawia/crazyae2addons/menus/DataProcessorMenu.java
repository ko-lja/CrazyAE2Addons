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
import net.oktawia.crazyae2addons.Parts.DataExtractorPart;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;
import net.oktawia.crazyae2addons.screens.DataExtractorScreen;

import java.util.List;
import java.util.Map;

public class DataProcessorMenu extends UpgradeableMenu<DataProcessorBE> implements IUpgradeableObject {

    public final String OPEN_SUB = "actionOpenSubmenu";
    public final String SAVE_INOUT = "actionSaveInOut";
    public String in;
    public String out;

    public DataProcessorMenu(int id, Inventory ip, DataProcessorBE host) {
        super(Menus.DATA_PROCESSOR_MENU, id, ip, host);
        getHost().setMenu(this);
        List<ItemStack> allowedItems = Items.getCards().stream().map(ItemDefinition::stack).toList();
        for (int i = 0; i < getHost().getInternalInventory().size(); i ++){
            this.addSlot(new AppEngManyFilteredSlot(getHost().inv, i, allowedItems), SlotSemantics.STORAGE);
        }
        registerClientAction(OPEN_SUB, Integer.class, this::openSubMenu);
        registerClientAction(SAVE_INOUT, String.class, this::save);
        this.in = getHost().in;
        this.out = getHost().out;
    }

    public void save(String data){
        this.getHost().in = data.split("\\|")[0];
        this.getHost().out = data.split("\\|")[1];
        this.in = data.split("\\|")[0];
        this.out = data.split("\\|")[1];
        if (isClientSide()){
            sendClientAction(SAVE_INOUT, data);
        } else {
            this.getHost().notifyDatabase();
        }
    }

    public void openSubMenu(Integer index) {
        getHost().submenuNum = index;
        if(isClientSide()){
            sendClientAction(OPEN_SUB, index);
        } else {
            MenuOpener.open(Menus.DATA_PROCESSOR_SUB_MENU, getPlayer(), MenuLocators.forBlockEntity(getBlockEntity()), true);
        }
    }
}
