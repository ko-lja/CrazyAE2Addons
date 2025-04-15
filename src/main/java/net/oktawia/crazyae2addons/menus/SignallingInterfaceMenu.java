package net.oktawia.crazyae2addons.menus;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.logic.Signalling.SignallingInterfaceLogicHost;

public class SignallingInterfaceMenu extends UpgradeableMenu<SignallingInterfaceLogicHost> {

    public static final String ACTION_OPEN_SET_AMOUNT = "setAmount";

    public SignallingInterfaceMenu(int id, Inventory ip, SignallingInterfaceLogicHost host) {
        super(Menus.SIGNALLING_INTERFACE_MENU, id, ip, host);

        registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);

        var logic = host.getInterfaceLogic();

        var config = logic.getConfig().createMenuWrapper();
        for (int x = 0; x < config.size(); x++) {
            this.addSlot(new FakeSlot(config, x), SlotSemantics.CONFIG);
        }

        var storage = logic.getStorage().createMenuWrapper();
        for (int x = 0; x < storage.size(); x++) {
            this.addSlot(new AppEngSlot(storage, x), SlotSemantics.STORAGE);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
    }

    public void openSetAmountMenu(int configSlot) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
        } else {
            var stack = getHost().getConfig().getStack(configSlot);
            if (stack != null) {
                SetStockAmountMenu.open((ServerPlayer) getPlayer(), getLocator(), configSlot,
                    stack.what(), (int) stack.amount());
            }
        }
    }
}
