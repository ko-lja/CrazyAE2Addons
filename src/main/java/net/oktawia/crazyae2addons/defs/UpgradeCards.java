package net.oktawia.crazyae2addons.defs;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

public class UpgradeCards {
    public UpgradeCards(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.SPEED_CARD, CrazyItemRegistrar.ENTITY_TICKER_PART_ITEM.get(), 8, "group.entity_ticker.name");
            Upgrades.add(AEItems.SPEED_CARD, CrazyItemRegistrar.ENERGY_EXPORTER_PART_ITEM.get(), 4, "group.energy_exporter.name");
            Upgrades.add(AEItems.SPEED_CARD, CrazyItemRegistrar.RIGHT_CLICK_PROVIDER_PART_ITEM.get(), 4, "group.right_click_provider.name");
            Upgrades.add(AEItems.SPEED_CARD, CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK_ITEM.get(), 4, "group.mob_farm_controller.name");
            Upgrades.add(CrazyItemRegistrar.LOOTING_UPGRADE_CARD.get(), CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK_ITEM.get(), 3, "group.mob_farm_controller.name");
            Upgrades.add(CrazyItemRegistrar.EXPERIENCE_UPGRADE_CARD.get(), CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK_ITEM.get(), 2, "group.mob_farm_controller.name");
            Upgrades.add(AEItems.SPEED_CARD, CrazyBlockRegistrar.SPAWNER_CONTROLLER_WALL_BLOCK_ITEM.get(), 4, "group.spawner_controller_wall.name");
            Upgrades.add(AEItems.REDSTONE_CARD, CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK_ITEM.get(), 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.FUZZY_CARD, CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK_ITEM.get(), 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.INVERTER_CARD, CrazyBlockRegistrar.SIGNALLING_INTERFACE_BLOCK_ITEM.get(), 1, "group.signalling_interface.name");
        });
    }
}