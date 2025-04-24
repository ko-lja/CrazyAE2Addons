package net.oktawia.crazyae2addons.defs;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class UpgradeCards {
    public UpgradeCards(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.SPEED_CARD, Items.ENTITY_TICKER_PART_ITEM, 8, "group.entity_ticker.name");
            Upgrades.add(AEItems.SPEED_CARD, Items.ENERGY_EXPORTER_PART_ITEM, 4, "group.energy_exporter.name");
            Upgrades.add(AEItems.SPEED_CARD, Items.RIGHT_CLICK_PROVIDER_PART_ITEM, 4, "group.right_click_provider.name");
            Upgrades.add(AEItems.REDSTONE_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.FUZZY_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.INVERTER_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");
            if (ModList.get().isLoaded("gtceu")){
                Upgrades.add(Items.CIRCUIT_UPGRADE_CARD_ITEM, AEParts.STORAGE_BUS, 1, "group.storage_bus.name");
            }
        });
    }
}