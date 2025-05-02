package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MobFarmBE;
import net.oktawia.crazyae2addons.misc.AppEngMobFilteredFakeSlot;

public class MobFarmMenu extends UpgradeableMenu<MobFarmBE> {
    public MobFarmCluster cluster;

    @GuiSync(348)
    public Integer damageBlocks;

    public MobFarmMenu(int id, Inventory ip, MobFarmBE host) {
        super(CrazyMenuRegistrar.MOB_FARM_MENU.get(), id, ip, host);
        this.cluster = host.cluster;
        this.damageBlocks = cluster.damageBlocks * 100 / 16;
        for (int x = 0; x < cluster.getConfigInventory().size(); x++) {
            this.addSlot(new AppEngMobFilteredFakeSlot(cluster.getConfigInventory().createMenuWrapper(), x), SlotSemantics.CONFIG);
        }
        this.addSlot(new AppEngSlot(cluster.getInventory(), 0), SlotSemantics.STORAGE);
    }
}