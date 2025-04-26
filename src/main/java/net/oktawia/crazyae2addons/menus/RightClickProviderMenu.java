package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.parts.RightClickProviderPart;

public class RightClickProviderMenu extends UpgradeableMenu<RightClickProviderPart> {

    public RightClickProviderMenu(int id, Inventory ip, RightClickProviderPart host) {
        super(CrazyMenuRegistrar.RIGHT_CLICK_PROVIDER_MENU.get(), id, ip, host);
        this.addSlot(new AppEngSlot(host.inv, 0), SlotSemantics.STORAGE);
    }
}
