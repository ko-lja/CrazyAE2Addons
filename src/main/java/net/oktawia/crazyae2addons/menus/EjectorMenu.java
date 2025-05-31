package net.oktawia.crazyae2addons.menus;

import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.CraftingCancelerBE;
import net.oktawia.crazyae2addons.entities.EjectorBE;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;

public class EjectorMenu extends UpgradeableMenu<EjectorBE> {

    public EjectorMenu(int id, Inventory ip, EjectorBE host) {
        super(CrazyMenuRegistrar.EJECTOR_MENU.get(), id, ip, host);
        this.addSlot(new AppEngFilteredSlot (host.inv, 0, AEItems.PROCESSING_PATTERN.asItem()), SlotSemantics.ENCODED_PATTERN);
    }
}
