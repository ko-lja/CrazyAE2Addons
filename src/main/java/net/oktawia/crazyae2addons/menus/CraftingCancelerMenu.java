package net.oktawia.crazyae2addons.menus;

import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.CraftingCancelerBE;

public class CraftingCancelerMenu extends UpgradeableMenu<CraftingCancelerBE> {

    @GuiSync(38)
    public boolean en;

    @GuiSync(94)
    public int dur;

    private static final String ACTION_SEND_STATE = "ActionSendState";
    private static final String ACTION_SEND_DURATION = "ActionSendDuration";

    public CraftingCancelerMenu(int id, Inventory ip, CraftingCancelerBE host) {
        super(CrazyMenuRegistrar.CRAFTING_CANCELER_MENU.get(), id, ip, host);
        en = getHost().getEnabled();
        dur = getHost().getDuration();

        registerClientAction(ACTION_SEND_STATE, Boolean.class, this::sendState);
        registerClientAction(ACTION_SEND_DURATION, Integer.class, this::sendDuration);
    }

    public void setEnabled(boolean en){
        this.en = en;
    }

    public void setDuration(int dur){
        this.dur = dur;
    }

    public void sendState(boolean state){
        setEnabled(state);
        if (isClientSide()){
            sendClientAction(ACTION_SEND_STATE, state);
        }
        else{
            this.getHost().setEnabled(state);
        }
    }

    public void sendDuration(int duration){
        setDuration(duration);
        if (isClientSide()){
            sendClientAction(ACTION_SEND_DURATION, duration);
        }
        else{
            this.getHost().setDuration(duration);
        }
    }
}
