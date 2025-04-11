package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.parts.ChunkyFluidP2PTunnelPart;
import net.oktawia.crazyae2addons.parts.DisplayPart;


public class ChunkyFluidP2PTunnelMenu extends AEBaseMenu {

    @GuiSync(145)
    public Integer value;

    public String ACTION_SYNC_VALUE = "syncValue";
    public ChunkyFluidP2PTunnelPart host;

    public ChunkyFluidP2PTunnelMenu(int id, Inventory ip, ChunkyFluidP2PTunnelPart host) {
        super(Menus.CHUNKY_FLUID_P2P_TUNNEL_MENU, id, ip, host);
        this.host = host;
        this.value = host.unitSize;
        registerClientAction(ACTION_SYNC_VALUE, Integer.class, this::syncValue);
    }

    public void syncValue(Integer value) {
        this.value = value;
        host.unitSize = value;
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_VALUE, value);
        }
        this.host.getHost().markForSave();
    }
}
