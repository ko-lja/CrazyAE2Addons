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
import net.oktawia.crazyae2addons.defs.ItemDefs;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;

import java.util.List;

public class DataProcessorMenu extends UpgradeableMenu<DataProcessorBE> implements IUpgradeableObject {

    public final String OPEN_SUB = "actionOpenSubmenu";
    public final String SAVE_INOUT = "actionSaveInOut";

    @GuiSync(564)
    public String in;
    @GuiSync(832)
    public boolean looped = false;


    public DataProcessorMenu(int id, Inventory ip, DataProcessorBE host) {
        super(CrazyMenuRegistrar.DATA_PROCESSOR_MENU.get(), id, ip, host);
        this.in = getHost().in;
        this.looped = host.looped;
        getHost().setMenu(this);
        List<ItemStack> allowedItems = CrazyItemRegistrar.getCards().stream().map(x -> x.asItem().getDefaultInstance()).toList();
        for (int i = 0; i < getHost().getInternalInventory().size(); i ++){
            this.addSlot(new AppEngManyFilteredSlot(getHost().inv, i, allowedItems), SlotSemantics.STORAGE);
        }
        registerClientAction(OPEN_SUB, Integer.class, this::openSubMenu);
        registerClientAction(SAVE_INOUT, String.class, this::save);
    }

    public void save(String data){
        this.looped = false;
        this.getHost().looped = false;
        this.getHost().in = data;
        this.in = data;
        this.getHost().markForUpdate();
        if (isClientSide()){
            sendClientAction(SAVE_INOUT, data);
        } else {
            this.getHost().notifyDatabase();
        }
    }

    public void openSubMenu(Integer index) {
        getHost().submenuNum = index;
        getHost().in = this.in;
        if(isClientSide()){
            sendClientAction(OPEN_SUB, index);
        } else {
            MenuOpener.open(CrazyMenuRegistrar.DATA_PROCESSOR_SUB_MENU.get(), getPlayer(), MenuLocators.forBlockEntity(getBlockEntity()), true);
        }
    }
}
