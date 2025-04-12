package net.oktawia.crazyae2addons.defs;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class UpgradeCards {
    public UpgradeCards(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.SPEED_CARD, Items.ENTITY_TICKER_PART_ITEM, 8, "group.entity_ticker.name");
            Upgrades.add(AEItems.SPEED_CARD, Blocks.AUTO_ENCHANTER_BLOCK, 4, "group.auto_enchanter.name");
            Upgrades.add(AEItems.SPEED_CARD, Items.ENERGY_EXPORTER_PART_ITEM, 4, "group.energy_exporter.name");
        });
    }
}