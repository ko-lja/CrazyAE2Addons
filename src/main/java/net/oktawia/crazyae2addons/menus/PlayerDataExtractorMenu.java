package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;
import net.oktawia.crazyae2addons.parts.PlayerDataExtractorPart;

import java.util.UUID;

public class PlayerDataExtractorMenu extends AEBaseMenu {

    private final PlayerDataExtractorPart host;
    @GuiSync(874)
    public String available;
    @GuiSync(875)
    public Integer selected;
    @GuiSync(876)
    public String valueName;
    @GuiSync(743)
    public Integer page = 0;
    @GuiSync(421)
    public Integer delay;
    @GuiSync(426)
    public Boolean updateGui = false;
    @GuiSync(492)
    public boolean playerMode;
    @GuiSync(493)
    public String boundPlayer;

    public String ACTION_SYNC_SELECTED = "actionSyncSelected";
    public String ACTION_GET_DATA = "actionGetData";
    public String ACTION_SAVE_NAME = "actionSaveName";
    public String ACTION_SAVE_DELAY = "actionSaveDelay";
    public String TOGGLE_PLAYER = "actionTogglePlayer";
    public String BIND_PLAYER = "actionBindPlayer";

    public PlayerDataExtractorMenu(int id, Inventory ip, PlayerDataExtractorPart host) {
        super(CrazyMenuRegistrar.PLAYER_DATA_EXTRACTOR_MENU.get(), id, ip, host);
        host.setMenu(this);
        this.host = host;
        this.playerMode = host.playerMode;
        this.boundPlayer = host.target.toString();
        registerClientAction(ACTION_SYNC_SELECTED, Integer.class, this::syncValue);
        registerClientAction(ACTION_GET_DATA, this::getData);
        registerClientAction(ACTION_SAVE_NAME, String.class, this::saveName);
        registerClientAction(ACTION_SAVE_DELAY, Integer.class, this::saveDelay);
        registerClientAction(TOGGLE_PLAYER, this::togglePlayerMode);
        registerClientAction(BIND_PLAYER, String.class, this::bindPlayer);
        this.available = String.join("|", host.available);
        this.selected = host.selected;
        this.valueName = host.valueName;
        this.delay = host.delay;
        createPlayerInventorySlots(ip);
    }

    public void syncValue(Integer value) {
        host.selected = value;
        this.selected = value;
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_SELECTED, value);
        }
    }

    public void getData(){
        host.extractPossibleData();
        this.available = String.join("|", host.available);
        this.selected = host.selected;
        this.updateGui = true;
        if (isClientSide()){
            sendClientAction(ACTION_GET_DATA);
        }
    }

    public void saveName(String name) {
        host.valueName = name;
        this.valueName = name;
        if (isClientSide()){
            sendClientAction(ACTION_SAVE_NAME, name);
        }
    }

    public void saveDelay(Integer delay) {
        host.delay = delay;
        this.delay = delay;
        if (isClientSide()){
            sendClientAction(ACTION_SAVE_DELAY, delay);
        }
    }

    public void togglePlayerMode() {
        this.playerMode = !this.playerMode;
        this.host.playerMode = !this.host.playerMode;
        if (isClientSide()){
            sendClientAction(TOGGLE_PLAYER);
        }
    }

    public void bindPlayer(String player) {
        this.boundPlayer = player;
        this.host.target = UUID.fromString(player);
        if (isClientSide()){
            sendClientAction(BIND_PLAYER, player);
        }
    }
}
