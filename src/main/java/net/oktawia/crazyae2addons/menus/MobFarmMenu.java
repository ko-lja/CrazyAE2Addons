package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MobFarmControllerBE;
import net.oktawia.crazyae2addons.misc.AppEngMobFilteredFakeSlot;

public class MobFarmControllerMenu extends UpgradeableMenu<MobFarmControllerBE> {

    @GuiSync(462)
    public Integer damageBlocks;

    public MobFarmControllerMenu(int id, Inventory playerInventory, MobFarmControllerBE host) {
        super(CrazyMenuRegistrar.MOB_FARM_CONTROLLER_MENU.get(), id, playerInventory, host);
        this.damageBlocks = host.damageBlocks * 100 / 16 ;
        var config = host.config;
        for (int x = 0; x < config.size(); x++) {
            this.addSlot(new AppEngMobFilteredFakeSlot(config.createMenuWrapper(), x), SlotSemantics.CONFIG);
        }
        this.addSlot(new AppEngSlot(getHost().inv, 0), SlotSemantics.STORAGE);
    }
}