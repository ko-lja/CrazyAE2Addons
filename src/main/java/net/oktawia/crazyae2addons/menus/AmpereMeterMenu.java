package net.oktawia.crazyae2addons.menus;

import appeng.api.config.RelativeDirection;
import appeng.api.config.Settings;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.AmpereMeterBE;
import net.oktawia.crazyae2addons.parts.DisplayPart;


public class AmpereMeterMenu extends AEBaseMenu {

    public AmpereMeterBE host;
    @GuiSync(48)
    public boolean direction;
    @GuiSync(49)
    public String transfer = "-";
    @GuiSync(51)
    public String unit = "-";

    public String CHANGE_DIRECTION = "actionChangeDirection";

    public AmpereMeterMenu(int id, Inventory ip, AmpereMeterBE host) {
        super(Menus.AMPERE_METER_MENU, id, ip, host);
        this.host = host;
        this.host.setMenu(this);
        this.direction = host.direction;
        registerClientAction(CHANGE_DIRECTION, Boolean.class, this::changeDirection);
    }

    public void changeDirection(boolean dir) {
        this.host.direction = dir;
        this.direction = dir;
        if (isClientSide()){
            sendClientAction(CHANGE_DIRECTION, dir);
        }
    }
}
