package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MobFarmControllerBE;
import net.oktawia.crazyae2addons.misc.AppEngMobFilteredFakeSlot;

public class MobFarmControllerMenu extends UpgradeableMenu<MobFarmControllerBE> {
    @GuiSync(348)
    public Integer damageBlocks;

    public MobFarmControllerMenu(int id, Inventory ip, MobFarmControllerBE host) {
        super(CrazyMenuRegistrar.MOB_FARM_CONTROLLER_MENU.get(), id, ip, host);
        this.damageBlocks = host.damageBlocks * 100 / 16;
        for (int x = 0; x < host.configInventory.size(); x++) {
            this.addSlot(new FakeSlot(host.configInventory.createMenuWrapper(), x), SlotSemantics.CONFIG);
        }
        this.addSlot(new AppEngSlot(host.inventory, 0), SlotSemantics.STORAGE);
    }
}
