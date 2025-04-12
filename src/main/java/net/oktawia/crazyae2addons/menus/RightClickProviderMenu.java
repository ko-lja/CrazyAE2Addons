package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;
import net.oktawia.crazyae2addons.parts.EnergyExporterPart;
import net.oktawia.crazyae2addons.parts.RightClickProviderPart;

import java.util.List;

public class RightClickProviderMenu extends UpgradeableMenu<RightClickProviderPart> {

    public RightClickProviderMenu(int id, Inventory ip, RightClickProviderPart host) {
        super(Menus.RIGHT_CLICK_PROVIDER_MENU, id, ip, host);
        this.addSlot(new AppEngSlot(host.inv, 0), SlotSemantics.STORAGE);
    }
}
