package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.EjectorBE;

public class EjectorMenu extends UpgradeableMenu<EjectorBE> {

    public EjectorMenu(int id, Inventory ip, EjectorBE host) {
        super(CrazyMenuRegistrar.EJECTOR_MENU.get(), id, ip, host);
        for (int i = 0; i < host.config.size(); i++){
            this.addSlot(new FakeSlot(host.config.createMenuWrapper(), i), SlotSemantics.CONFIG);
        }
    }
}
