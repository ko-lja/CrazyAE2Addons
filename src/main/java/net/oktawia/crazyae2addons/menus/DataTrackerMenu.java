package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.DataTrackerBE;
import net.oktawia.crazyae2addons.parts.DisplayPart;


public class DataTrackerMenu extends AEBaseMenu {

    @GuiSync(543)
    public String variable;

    public String ACTION_SYNC_VARIABLE = "syncVariable";
    public DataTrackerBE host;

    public DataTrackerMenu(int id, Inventory ip, DataTrackerBE host) {
        super(Menus.DATA_TRACKER_MENU, id, ip, host);
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
