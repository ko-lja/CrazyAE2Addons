package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.EnergyStorageControllerBE;

public class EnergyStorageControllerMenu extends AEBaseMenu {
    @GuiSync(72)
    public long energy;
    @GuiSync(73)
    public long maxEnergy;

    public EnergyStorageControllerMenu(
            int id, Inventory ip, EnergyStorageControllerBE host) {
        super(CrazyMenuRegistrar.ENERGY_STORAGE_CONTROLLER_MENU.get(), id, ip, host);
        host.setMenu(this);
        this.energy = (long) host.energy;
        this.maxEnergy = (long) host.maxEnergy;
        this.createPlayerInventorySlots(ip);
    }
}