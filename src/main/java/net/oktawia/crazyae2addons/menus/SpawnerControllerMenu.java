package net.oktawia.crazyae2addons.menus;

import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.clusters.SpawnerControllerCluster;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;

public class SpawnerControllerMenu extends UpgradeableMenu<SpawnerControllerBE> {
    public SpawnerControllerCluster cluster;

    public SpawnerControllerMenu(int id, Inventory ip, SpawnerControllerBE host) {
        super(CrazyMenuRegistrar.SPAWNER_CONTROLLER_MENU.get(), id, ip, host);
        this.cluster = host.cluster;
    }
}