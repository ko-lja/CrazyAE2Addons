package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.EnergyStorageControllerBE;

public class EnergyStorageControllerMenu extends AEBaseMenu {
    private final EnergyStorageControllerBE host;
    @GuiSync(72)
    public long energy;
    @GuiSync(73)
    public long maxEnergy;
    @GuiSync(893)
    public boolean preview;
    public String PREVIEW = "actionPrev";

    public EnergyStorageControllerMenu(
            int id, Inventory ip, EnergyStorageControllerBE host) {
        super(CrazyMenuRegistrar.ENERGY_STORAGE_CONTROLLER_MENU.get(), id, ip, host);
        host.setMenu(this);
        this.energy = (long) host.energy;
        this.maxEnergy = (long) host.maxEnergy;
        this.host = host;
        this.preview = host.preview;
        this.registerClientAction(PREVIEW, Boolean.class, this::changePreview);
        this.createPlayerInventorySlots(ip);
    }

    public void changePreview(Boolean preview) {
        host.preview = preview;
        this.preview = preview;
        if (isClientSide()){
            sendClientAction(PREVIEW, preview);
        }
    }
}