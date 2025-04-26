package net.oktawia.crazyae2addons.menus;

import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;

public class MEDataControllerMenu extends UpgradeableMenu<MEDataControllerBE> {

    @GuiSync(874)
    public int variableNum = 0;
    @GuiSync(875)
    public int maxVariables = 0;
    public MEDataControllerMenu(int id, Inventory ip, MEDataControllerBE host) {
        super(CrazyMenuRegistrar.ME_DATA_CONTROLLER_MENU.get(), id, ip, host);
        this.getHost().setMenu(this);
        for(int i = 0; i < 6; i = i + 1){
            this.addSlot(new AppEngSlot(getHost().inv, i), SlotSemantics.STORAGE);
        }
        variableNum = getVariableNum();
        maxVariables = getMaxVariables();
    }

    public int getVariableNum() {
        return getHost().variables.size();
    }

    public int getMaxVariables() {
        int maxVariables = 0;
        InternalInventory cellInv = getHost().getInternalInventory();
        for (ItemStack stack : cellInv){
            if (stack.getItem() == AEItems.CELL_COMPONENT_1K.asItem()){
                maxVariables = maxVariables + 1;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_4K.asItem()) {
                maxVariables = maxVariables + 4;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_16K.asItem()) {
                maxVariables = maxVariables + 16;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_64K.asItem()) {
                maxVariables = maxVariables + 64;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_256K.asItem()) {
                maxVariables = maxVariables + 256;
            }
        }
        return maxVariables;
    }
}
