package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.DataSetterBE;

public class DataSetterMenu extends AEBaseMenu {

    private final String SYNC_VALUE = "actionSyncValue";
    private final String SYNC_VARIABLE = "actionSyncVariable";
    private final DataSetterBE host;
    @GuiSync(1)
    public String variableToSet;
    @GuiSync(2)
    public Integer valueToSet;

    public DataSetterMenu(int id, Inventory ip, DataSetterBE host) {
        super(CrazyMenuRegistrar.DATA_SETTER_MENU.get(), id, ip, host);
        this.host = host;
        this.variableToSet = host.variableToSet;
        this.valueToSet = host.valueToSet;
        this.registerClientAction(SYNC_VALUE, Integer.class, this::syncValue);
        this.registerClientAction(SYNC_VARIABLE, String.class, this::syncVariable);
    }

    public void syncVariable(String variable) {
        host.variableToSet = variable;
        this.variableToSet = variable;
        if (isClientSide()){
            sendClientAction(SYNC_VARIABLE, variable);
        }
    }

    public void syncValue(Integer value) {
        host.valueToSet = value;
        this.valueToSet = value;
        if (isClientSide()){
            sendClientAction(SYNC_VALUE, value);
        }
    }
}
