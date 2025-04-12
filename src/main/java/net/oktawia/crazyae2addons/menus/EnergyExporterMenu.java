package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;
import net.oktawia.crazyae2addons.parts.EnergyExporterPart;

import java.util.List;

public class EnergyExporterMenu extends UpgradeableMenu<EnergyExporterPart> {

    @GuiSync(353)
    public int maxAmps;
    @GuiSync(313)
    public int voltage;
    @GuiSync(113)
    public String transfered;
    @GuiSync(319)
    public boolean greg;

    public EnergyExporterMenu(int id, Inventory ip, EnergyExporterPart host) {
        super(Menus.ENERGY_EXPORTER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        List<ItemStack> filter = List.of(
                GTItems.BATTERY_LV_LITHIUM.asStack(),
                GTItems.BATTERY_MV_LITHIUM.asStack(),
                GTItems.BATTERY_HV_LITHIUM.asStack(),
                GTItems.BATTERY_EV_VANADIUM.asStack(),
                GTItems.BATTERY_IV_VANADIUM.asStack(),
                GTItems.BATTERY_LuV_VANADIUM.asStack(),
                GTItems.BATTERY_ZPM_NAQUADRIA.asStack(),
                GTItems.BATTERY_UV_NAQUADRIA.asStack()
        );
        this.addSlot(new AppEngManyFilteredSlot(host.inv, 0, filter), SlotSemantics.STORAGE);
        this.maxAmps = host.maxAmps;
        this.voltage = host.voltage;
        this.transfered = host.transfered;
        this.greg = host.greg;
    }
}
