package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.AutoBuilderBE;
import net.oktawia.crazyae2addons.misc.UnifiedAutoBuilderSlot;

public class AutoBuilderMenu extends UpgradeableMenu<AutoBuilderBE> {

    public AutoBuilderMenu(int id, Inventory playerInventory, AutoBuilderBE host) {
        super(CrazyMenuRegistrar.AUTO_BUILDER_MENU.get(), id, playerInventory, host);
        this.addSlot(new UnifiedAutoBuilderSlot(host.inventory, 0), SlotSemantics.ENCODED_PATTERN);
    }
}
