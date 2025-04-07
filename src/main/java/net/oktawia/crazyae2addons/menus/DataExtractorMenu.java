package net.oktawia.crazyae2addons.menus;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Parts.DataExtractorPart;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.screens.DataExtractorScreen;

public class DataExtractorMenu extends UpgradeableMenu<DataExtractorPart> implements IUpgradeableObject {

    @GuiSync(874)
    public String available;
    @GuiSync(875)
    public Integer selected;
    @GuiSync(876)
    public String valueName;
    @GuiSync(743)
    public Integer page = 0;

    public DataExtractorScreen<?> screen;
    public String ACTION_SYNC_SELECTED = "actionSyncSelected";
    public String ACTION_GET_DATA = "actionGetData";
    public String ACTION_SAVE_NAME = "actionSaveName";

    public DataExtractorMenu(int id, Inventory ip, DataExtractorPart host) {
        super(Menus.DATA_EXTRACTOR_MENU, id, ip, host);
        getHost().setMenu(this);
        registerClientAction(ACTION_SYNC_SELECTED, Integer.class, this::syncValue);
        registerClientAction(ACTION_GET_DATA, this::getData);
        registerClientAction(ACTION_SAVE_NAME, String.class, this::saveName);
        this.available = String.join("|", getHost().available);
        this.selected = getHost().selected;
        this.valueName = getHost().valueName;
    }

    public void syncValue(Integer value) {
        getHost().selected = value;
        this.selected = value;
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_SELECTED, value);
        }
    }

    public void getData(){
        getHost().extractPossibleData();
        this.available = String.join("|", getHost().available);
        this.selected = getHost().selected;
        if (isClientSide()){
            this.screen.updateGui();
            sendClientAction(ACTION_GET_DATA);
        }
    }

    public void saveName(String name) {
        getHost().valueName = name;
        this.valueName = name;
        if (isClientSide()){
            sendClientAction(ACTION_SAVE_NAME, name);
        }
    }
}
