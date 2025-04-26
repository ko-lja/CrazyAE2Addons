package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.DataTrackerBE;


public class DataTrackerMenu extends AEBaseMenu {

    @GuiSync(543)
    public String variable;

    public String ACTION_SYNC_VARIABLE = "syncVariable";
    public DataTrackerBE host;

    public DataTrackerMenu(int id, Inventory ip, DataTrackerBE host) {
        super(CrazyMenuRegistrar.DATA_TRACKER_MENU.get(), id, ip, host);
        this.host = host;
        this.variable = host.trackedVariable;
        registerClientAction(ACTION_SYNC_VARIABLE, String.class, this::syncVariable);
    }

    public void syncVariable(String value) {
        this.variable = value;
        host.setTracked(value);
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_VARIABLE, value);
        }
        this.host.markForUpdate();
    }
}
