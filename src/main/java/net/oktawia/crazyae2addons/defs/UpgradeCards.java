package net.oktawia.crazyae2addons.defs;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.oktawia.crazyae2addons.IsModLoaded;

import java.util.function.Supplier;

public class UpgradeCards {

    private static final Supplier<ItemLike> ENTITY_TICKER_PART = () -> Items.ENTITY_TICKER_PART_ITEM.asItem();
    private static final Supplier<ItemLike> ENERGY_EXPORTER_PART = () -> Items.ENERGY_EXPORTER_PART_ITEM.asItem();
    private static final Supplier<ItemLike> RIGHT_CLICK_PROVIDER_PART = () -> Items.RIGHT_CLICK_PROVIDER_PART_ITEM.asItem();
    private static final Supplier<ItemLike> CIRCUIT_UPGRADE_CARD = () -> Items.CIRCUIT_UPGRADE_CARD_ITEM != null ? Items.CIRCUIT_UPGRADE_CARD_ITEM.asItem() : null;

    public UpgradeCards(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.SPEED_CARD, ENTITY_TICKER_PART.get(), 8, "group.entity_ticker.name");
            Upgrades.add(AEItems.SPEED_CARD, ENERGY_EXPORTER_PART.get(), 4, "group.energy_exporter.name");
            Upgrades.add(AEItems.SPEED_CARD, RIGHT_CLICK_PROVIDER_PART.get(), 4, "group.right_click_provider.name");
            Upgrades.add(AEItems.REDSTONE_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.FUZZY_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");
            Upgrades.add(AEItems.INVERTER_CARD, Blocks.SIGNALLING_INTERFACE_BLOCK, 1, "group.signalling_interface.name");

            ItemLike circuitCard = CIRCUIT_UPGRADE_CARD.get();
            if (circuitCard != null) {
                Upgrades.add(circuitCard, AEParts.STORAGE_BUS, 1, "group.storage_bus.name");
            }
        });
    }
}