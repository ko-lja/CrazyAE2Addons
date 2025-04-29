package net.oktawia.crazyae2addons.mobstorage;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;

public class MobExportBusMenu extends UpgradeableMenu<MobExportBus> {
    public MobExportBusMenu(int id, Inventory playerInventory, MobExportBus host) {
        super(CrazyMenuRegistrar.MOB_EXPORT_BUS_MENU.get(), id, playerInventory, host);
        var config = host.config;
        for (int x = 0; x < config.size(); x++) {
            this.addSlot(new FakeSlot(config.createMenuWrapper(), x), SlotSemantics.CONFIG);
        }
    }
}
