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
import net.oktawia.crazyae2addons.parts.*;
import net.oktawia.crazyae2addons.items.*;

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

    public static final ItemDefinition<DisplayPartItem> DISPLAY_MNITOR_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(DisplayPart.class));
        return item("Display Monitor", "display_monitor", DisplayPartItem::new);
    });

    public static final ItemDefinition<DataExtractorPartItem> DATA_EXTRACTOR_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(DataExtractorPart.class));
        return item("Data Extractor", "data_extractor", DataExtractorPartItem::new);
    });

    public static final ItemDefinition<ChunkyFluidP2PTunnelPartItem> CHUNKY_FLUID_P2P_TUNNEL_PART = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ChunkyFluidP2PTunnelPart.class));
        return item("Chunky Fluid P2P Tunnel", "chunky_fluid_p2p_tunnel", ChunkyFluidP2PTunnelPartItem::new);
    });

    public static final ItemDefinition<EnergyExporterPartItem> ENERGY_EXPORTER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(EnergyExporterPart.class));
        return item("Energy Exporter", "energy_exporter", EnergyExporterPartItem::new);
    });

    public static final ItemDefinition<RightClickProviderPartItem> RIGHT_CLICK_PROVIDER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(RightClickProviderPart.class));
        return item("Right Click Provider", "rc_provider", RightClickProviderPartItem::new);
    });

    public static final ItemDefinition<CrazyPatternModifierItem> CRAZY_PATTERN_MODIFIER_ITEM = Util.make(() -> item(
            "Crazy Pattern Modifier", "crazy_pattern_modifier", CrazyPatternModifierItem::new));
    public static final ItemDefinition<CrazyPatternMultiplierItem> CRAZY_PATTERN_MULTIPLIER_ITEM = Util.make(() -> item(
            "Crazy Pattern Multiplier", "crazy_pattern_multiplier", CrazyPatternMultiplierItem::new));
    public static final ItemDefinition<CircuitUpgradeCard> CIRCUIT_UPGRADE_CARD_ITEM = Util.make(() -> item(
            "Circuit Upgrade Card", "circuit_upgrade_card", CircuitUpgradeCard::new));

    public static final ItemDefinition<XpShardItem> XP_SHARD_ITEM = Util.make(() -> item("XP Shard", "xp_shard", XpShardItem::new));

    private static final List<ItemDefinition<?>> CARDS = new ArrayList<>();
    public static final ItemDefinition<LogicCard> LOGIC_CARD = Util.make(() -> logicCard("Logic Card", "logic_card", LogicCard::new));
    public static final ItemDefinition<AddCard> ADD_CARD = Util.make(() -> logicCard("ADD Logic Card", "add_card", AddCard::new));
    public static final ItemDefinition<SubCard> SUB_CARD = Util.make(() -> logicCard("SUB Logic Card", "sub_card", SubCard::new));
    public static final ItemDefinition<MulCard> MUL_CARD = Util.make(() -> logicCard("MUL Logic Card", "mul_card", MulCard::new));
    public static final ItemDefinition<DivCard> DIV_CARD = Util.make(() -> logicCard("DIV Logic Card", "div_card", DivCard::new));
    public static final ItemDefinition<MinCard> MIN_CARD = Util.make(() -> logicCard("MIN Logic Card", "min_card", MinCard::new));
    public static final ItemDefinition<MaxCard> MAX_CARD = Util.make(() -> logicCard("MAX Logic Card", "max_card", MaxCard::new));
    public static final ItemDefinition<BsrCard> BSR_CARD = Util.make(() -> logicCard("BSR Logic Card", "bsr_card", BsrCard::new));
    public static final ItemDefinition<BslCard> BSL_CARD = Util.make(() -> logicCard("BSL Logic Card", "bsl_card", BslCard::new));
    public static final ItemDefinition<HitCard> HIT_CARD = Util.make(() -> logicCard("HIT Logic Card", "hit_card", HitCard::new));
    public static final ItemDefinition<HifCard> HIF_CARD = Util.make(() -> logicCard("HIF Logic Card", "hif_card", HifCard::new));


    public static List<ItemDefinition<?>> getCards() {
        return Collections.unmodifiableList(CARDS);
    }
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

    public static <T extends Item> ItemDefinition<T> logicCard(
            String englishName, String id, Function<Item.Properties, T> factory) {
        var def = item(englishName, id, factory);
        CARDS.add(def);
        return def;
    }
}