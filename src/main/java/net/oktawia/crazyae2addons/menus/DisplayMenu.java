package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Parts.DisplayPart;
import net.oktawia.crazyae2addons.defs.Menus;


public class DisplayMenu extends AEBaseMenu {

    @GuiSync(145)
    public String displayValue;

    public String ACTION_SYNC_DISPLAY_VALUE = "syncDisplayValue";
    public DisplayPart host;

    public DisplayMenu(int id, Inventory ip, DisplayPart host) {
        super(Menus.DISPLAY_MENU, id, ip, host);
        this.host = host;
        this.displayValue = host.textValue;
        registerClientAction(ACTION_SYNC_DISPLAY_VALUE, String.class, this::syncValue);
    }

    public void syncValue(String value) {
        host.textValue = value;
        this.displayValue = value;
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_DISPLAY_VALUE, value);
        }
        this.host.getHost().markForSave();
    }
}
