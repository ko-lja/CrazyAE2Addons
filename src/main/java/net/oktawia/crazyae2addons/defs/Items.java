package net.oktawia.crazyae2addons.defs;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.Parts.EntityTickerPart;
import net.oktawia.crazyae2addons.Parts.NBTExportBusPart;
import net.oktawia.crazyae2addons.Parts.RRItemP2PTunnelPart;
import net.oktawia.crazyae2addons.items.CraftingCancelerBlockItem;
import net.oktawia.crazyae2addons.items.EntityTickerPartItem;
import net.oktawia.crazyae2addons.items.NBTExportBusPartItem;
import net.oktawia.crazyae2addons.items.RRItemP2PTunnelPartItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Items {

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final ItemDefinition<EntityTickerPartItem> ENTITY_TICKER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(EntityTickerPart.class));
        return item("Entity Ticker", "entity_ticker", EntityTickerPartItem::new);
    });

    public static final ItemDefinition<RRItemP2PTunnelPartItem> RR_ITEM_P2P_TUNNEL_PART = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(RRItemP2PTunnelPart.class));
        return item("RR Item P2P Tunnel", "rr_item_p2p_tunnel", RRItemP2PTunnelPartItem::new);
    });

    public static final ItemDefinition<NBTExportBusPartItem> NBT_EXPORT_BUS_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(NBTExportBusPart.class));
        return item("NBT Export Bus", "nbt_export_bus", NBTExportBusPartItem::new);
    });

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static <T extends IPart> ItemDefinition<PartItem<T>> part(
            String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, p -> new PartItem<>(p, partClass, factory));
    }

    public static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory) {
        var definition = new ItemDefinition<>(englishName, CrazyAddons.makeId(id), factory.apply(new Item.Properties()));
        ITEMS.add(definition);
        return definition;
    }
}