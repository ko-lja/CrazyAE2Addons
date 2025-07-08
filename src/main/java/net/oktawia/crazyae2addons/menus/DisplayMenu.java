package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.parts.DisplayPart;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;


public class DisplayMenu extends AEBaseMenu {

    @GuiSync(145)
    public String displayValue;
    @GuiSync(29)
    public boolean mode;
    @GuiSync(31)
    public int fontSize;

    public String ACTION_SYNC_DISPLAY_VALUE = "syncDisplayValue";
    public String MODE = "changeMode";
    public String CENTER = "changeCenter";
    public String FONT = "changeFont";
    public DisplayPart host;

    public DisplayMenu(int id, Inventory ip, DisplayPart host) {
        super(CrazyMenuRegistrar.DISPLAY_MENU.get(), id, ip, host);
        this.host = host;
        this.displayValue = host.textValue;
        this.mode = host.mode;
        this.fontSize = host.fontSize;
        registerClientAction(ACTION_SYNC_DISPLAY_VALUE, String.class, this::syncValue);
        registerClientAction(MODE, Boolean.class, this::changeMode);
        registerClientAction(FONT, String.class, this::setFont);
        createPlayerInventorySlots(ip);
    }

    public void syncValue(String value) {
        this.displayValue = value;
        host.updateController(value);
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_DISPLAY_VALUE, value);
        }
        this.host.getHost().markForSave();
    }

    public void changeMode(boolean btn) {
        this.mode = btn;
        host.mode = btn;
        if (isClientSide()){
            sendClientAction(MODE, btn);
        }
    }

    public void setFont(String val) {
        try {
            var num = Integer.parseInt(val);
            this.fontSize = num;
            host.fontSize = num;
            if (isClientSide()){
                sendClientAction(FONT, val);
            }
        } catch (Exception ignored) {}
    }
}
