package net.oktawia.crazyae2addons.menus;

import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.CraftingCancelerBE;

import java.util.logging.Logger;

public class CraftingCancelerMenu extends UpgradeableMenu<CraftingCancelerBE> {

    @GuiSync(38)
    public boolean en;

    @GuiSync(94)
    public int dur;

    private static final String ACTION_SEND_STATE = "ActionSendState";
    private static final String ACTION_SEND_DURATION = "ActionSendDuration";

    public CraftingCancelerMenu(int id, Inventory ip, CraftingCancelerBE host) {
        super(Menus.CRAFTING_CANCELER_MENU, id, ip, host);
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
