package net.oktawia.crazyae2addons.menus;

import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.SpawnerExtractorControllerBE;


public class SpawnerExtractorControllerMenu extends UpgradeableMenu<SpawnerExtractorControllerBE> {

    public SpawnerExtractorControllerMenu(int id, Inventory ip, SpawnerExtractorControllerBE host) {
        super(CrazyMenuRegistrar.SPAWNER_EXTRACTOR_CONTROLLER_MENU.get(), id, ip, host);
    }
}
