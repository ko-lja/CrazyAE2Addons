package net.oktawia.crazyae2addons.menus;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.slot.FakeSlot;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.DataProcessorBE;
import net.oktawia.crazyae2addons.misc.LogicSetting;
import net.oktawia.crazyae2addons.misc.NBTContainer;

import java.util.Map;

public class DataProcessorSubMenu extends UpgradeableMenu<DataProcessorBE> implements IUpgradeableObject {
    @GuiSync(374)
    public Integer submenuNum;
    @GuiSync(831)
    public String cardSettings;
    @GuiSync(891)
    public String valueIn;
    public final String SYNC_SETTINGS = "actionSyncSettings";
    public String CLOSE_SUBSCREEN = "actionCloseSubScreen";
    public boolean COMPRESSED = true;

    public DataProcessorSubMenu(int id, Inventory ip, DataProcessorBE host) {
        super(Menus.DATA_PROCESSOR_SUB_MENU, id, ip, host);
        this.submenuNum = getHost().submenuNum;
        var FSlot = new FakeSlot(getHost().getInternalInventory(), this.submenuNum);
        FSlot.set(getHost().getInternalInventory().getStackInSlot(submenuNum));
        this.addSlot(FSlot, SlotSemantics.STORAGE);
        registerClientAction(CLOSE_SUBSCREEN, this::closeSubScreen);
        registerClientAction(SYNC_SETTINGS, String.class, this::syncSettings);
        this.cardSettings = NBTContainer.serializeToString(getHost().settings, COMPRESSED);
        this.valueIn = getHost().in;
    }

    public void setSetting(String in1, String in2, String out){
        NBTContainer settingMap = NBTContainer.deserializeFromString(this.cardSettings, COMPRESSED);
        settingMap.set(String.valueOf(this.submenuNum), new LogicSetting(in1, in2, out));
        String settingsParsed = NBTContainer.serializeToString(settingMap, COMPRESSED);
        this.cardSettings = settingsParsed;
        syncSettings(settingsParsed);
    }

    public void syncSettings(String settings){
        this.getHost().settings = NBTContainer.deserializeFromString(settings, COMPRESSED);
        this.getHost().markForUpdate();
        if(isClientSide()){
            sendClientAction(SYNC_SETTINGS, settings);
        }
    }

    public void closeSubScreen() {
        if (isClientSide()){
            sendClientAction(CLOSE_SUBSCREEN);
        } else {
            MenuOpener.returnTo(Menus.DATA_PROCESSOR_MENU, getPlayer(), MenuLocators.forBlockEntity(getBlockEntity()));
        }
    }
}
