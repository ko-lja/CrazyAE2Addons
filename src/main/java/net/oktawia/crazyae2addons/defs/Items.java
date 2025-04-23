package net.oktawia.crazyae2addons.defs;

import appeng.api.parts.PartModels;
import appeng.core.definitions.*;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.parts.*;
import net.oktawia.crazyae2addons.items.*;

import java.util.*;
import java.util.function.Function;

public class Items {

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();
    private static final List<ItemDefinition<?>> CARDS = new ArrayList<>();
    private static final List<ItemDefinition<?>> PARTS = new ArrayList<>();
    private static final Map<ItemDefinition<?>, Map.Entry<String, Map<String, Item>>> ITEM_RECIPES = new HashMap<>();

    public static final ItemDefinition<LogicCard> LOGIC_CARD = Util.make(() ->
            logicCard(
                    "Logic Card",
                    "logic_card",
                    LogicCard::new,
                    "AS",
                    Map.of(
                            "A", AEItems.ADVANCED_CARD.asItem(),
                            "S", AEItems.SKY_DUST.asItem()
                    )
            ));

    public static final ItemDefinition<AddCard> ADD_CARD = Util.make(() ->
            logicCard(
                    "ADD Logic Card",
                    "add_card",
                    AddCard::new,
                    "DC",
                    Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<SubCard> SUB_CARD = Util.make(() ->
            logicCard(
                    "SUB Logic Card",
                    "sub_card",
                    SubCard::new,
                    "CD",
                    Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<MulCard> MUL_CARD = Util.make(() ->
            logicCard(
                    "MUL Logic Card",
                    "mul_card",
                    MulCard::new,
                    "D/C",
                    Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<DivCard> DIV_CARD = Util.make(() ->
            logicCard(
                    "DIV Logic Card",
                    "div_card",
                    DivCard::new,
                    "C/D",
                    Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<MaxCard> MAX_CARD = Util.make(() ->
            logicCard(
                    "MAX Logic Card",
                    "max_card",
                    MaxCard::new,
                    "DC",
                    Map.of(
                            "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<MinCard> MIN_CARD = Util.make(() ->
            logicCard(
                    "MIN Logic Card",
                    "min_card",
                    MinCard::new,
                    "CD",
                    Map.of(
                            "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<BsrCard> BSR_CARD = Util.make(() ->
            logicCard(
                    "BSR Logic Card",
                    "bsr_card",
                    BsrCard::new,
                    "D/C",
                    Map.of(
                            "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<BslCard> BSL_CARD = Util.make(() ->
            logicCard(
                    "BSL Logic Card",
                    "bsl_card",
                    BslCard::new,
                    "C/D",
                    Map.of(
                            "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                            "C", LOGIC_CARD.asItem()
                    )
            ));

    public static final ItemDefinition<HitCard> HIT_CARD = Util.make(() ->
            logicCard(
                    "HIT Logic Card",
                    "hit_card",
                    HitCard::new,
                    "DCR",
                    Map.of(
                            "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                            "C", LOGIC_CARD.asItem(),
                            "R", net.minecraft.world.item.Items.REDSTONE
                    )
            ));

    public static final ItemDefinition<HifCard> HIF_CARD = Util.make(() ->
            logicCard(
                    "HIF Logic Card",
                    "hif_card",
                    HifCard::new,
                    "DCR",
                    Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem(),
                            "R", net.minecraft.world.item.Items.REDSTONE
                    )
            ));

    public static final ItemDefinition<RRItemP2PTunnelPartItem> RR_ITEM_P2P_TUNNEL_PART = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(RRItemP2PTunnelPart.class));
        return part(
                "Round Robin Item P2P Tunnel",
                "rr_item_p2p_tunnel",
                RRItemP2PTunnelPartItem::new,
                "PE",
                Map.of(
                        "P", AEParts.ITEM_P2P_TUNNEL.asItem(),
                        "E", AEItems.EQUAL_DISTRIBUTION_CARD.asItem()
                )
        );
    });

    public static final ItemDefinition<NBTExportBusPartItem> NBT_EXPORT_BUS_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(NBTExportBusPart.class));
        return part(
                "NBT Export Bus",
                "nbt_export_bus",
                NBTExportBusPartItem::new,
                "ET/TL",
                Map.of(
                        "E", AEParts.EXPORT_BUS.asItem(),
                        "T", net.minecraft.world.item.Items.NAME_TAG,
                        "L", AEItems.LOGIC_PROCESSOR.asItem()
                )
        );
    });

    public static final ItemDefinition<DisplayPartItem> DISPLAY_MONITOR_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(DisplayPart.class));
        return part(
                "Display Monitor",
                "display_monitor",
                DisplayPartItem::new,
                "TL",
                Map.of(
                        "T", AEParts.MONITOR.asItem(),
                        "L", LOGIC_CARD.asItem()
                )
        );
    });

    public static final ItemDefinition<DataExtractorPartItem> DATA_EXTRACTOR_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(DataExtractorPart.class));
        return part(
                "Data Extractor",
                "data_extractor",
                DataExtractorPartItem::new,
                "IL",
                Map.of(
                        "I", AEParts.IMPORT_BUS.asItem(),
                        "L", LOGIC_CARD.asItem()
                )
        );
    });

    public static final ItemDefinition<ChunkyFluidP2PTunnelPartItem> CHUNKY_FLUID_P2P_TUNNEL_PART = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ChunkyFluidP2PTunnelPart.class));
        return part(
                "Chunky Fluid P2P Tunnel",
                "chunky_fluid_p2p_tunnel",
                ChunkyFluidP2PTunnelPartItem::new,
                "TL",
                Map.of(
                        "T", AEParts.FLUID_P2P_TUNNEL.asItem(),
                        "L", AEItems.LOGIC_PROCESSOR.asItem()
                )
        );
    });

    public static final ItemDefinition<EnergyExporterPartItem> ENERGY_EXPORTER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(EnergyExporterPart.class));
        return part(
                "Energy Exporter",
                "energy_exporter",
                EnergyExporterPartItem::new,
                "ERR",
                Map.of(
                        "E", AEParts.EXPORT_BUS.asItem(),
                        "R", net.minecraft.world.item.Items.REDSTONE
                )
        );
    });

    public static final ItemDefinition<EntityTickerPartItem> ENTITY_TICKER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(EntityTickerPart.class));
        return part(
                "Entity Ticker",
                "entity_ticker",
                EntityTickerPartItem::new,
                "DND/NEN/DND",
                Map.of(
                        "D", net.minecraft.world.item.Items.DIAMOND,
                        "N", net.minecraft.world.item.Items.NETHER_STAR,
                        "E", ENERGY_EXPORTER_PART_ITEM.asItem()
                )
        );
    });

    public static final ItemDefinition<RightClickProviderPartItem> RIGHT_CLICK_PROVIDER_PART_ITEM = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(RightClickProviderPart.class));
        return part(
                "Right Click Provider",
                "rc_provider",
                RightClickProviderPartItem::new,
                "ED/TP",
                Map.of(
                        "E", AEParts.EXPORT_BUS.asItem(),
                        "D", net.minecraft.world.item.Items.DIAMOND,
                        "T", net.minecraft.world.item.Items.NAME_TAG,
                        "P", AEItems.ENGINEERING_PROCESSOR.asItem()
                )
        );
    });

    public static final ItemDefinition<CrazyPatternModifierItem> CRAZY_PATTERN_MODIFIER_ITEM = Util.make(() ->
            item(
            "Crazy Pattern Modifier",
                    "crazy_pattern_modifier",
                    CrazyPatternModifierItem::new,
                    "PZ/ZP",
                    Map.of(
                            "P", AEItems.BLANK_PATTERN.asItem(),
                            "Z", AEItems.LOGIC_PROCESSOR.asItem()
                    )
            ));

    public static final ItemDefinition<CrazyPatternMultiplierItem> CRAZY_PATTERN_MULTIPLIER_ITEM = Util.make(() ->
            item(
            "Crazy Pattern Multiplier",
                    "crazy_pattern_multiplier",
                    CrazyPatternMultiplierItem::new,
                    "PZ/ZP",
                    Map.of(
                            "P", AEItems.BLANK_PATTERN.asItem(),
                            "Z", AEItems.CALCULATION_PROCESSOR.asItem()
                    )
            ));

    public static final ItemDefinition<CircuitUpgradeCard> CIRCUIT_UPGRADE_CARD_ITEM = Util.make(() ->
            item(
            "Circuit Upgrade Card",
                    "circuit_upgrade_card",
                    CircuitUpgradeCard::new,
                    "CT",
                    Map.of(
                            "C", AEItems.ADVANCED_CARD.asItem(),
                            "T", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
                    )
            ));

    public static final ItemDefinition<XpShardItem> XP_SHARD_ITEM = Util.make(() -> item("XP Shard", "xp_shard", XpShardItem::new));

    public static List<ItemDefinition<?>> getCards() {
        return Collections.unmodifiableList(CARDS);
    }
    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }
    public static List<ItemDefinition<?>> getParts() {
        return Collections.unmodifiableList(PARTS);
    }
    public static Map<ItemDefinition<?>, Map.Entry<String, Map<String, Item>>> getItemRecipes() {
        return ITEM_RECIPES;
    }


    public static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory, String recipe, Map<String, Item> recipe_map) {
        var definition = new ItemDefinition<>(englishName, CrazyAddons.makeId(id), factory.apply(new Item.Properties()));
        ITEMS.add(definition);
        ITEM_RECIPES.put(definition, Map.entry(recipe, recipe_map));
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory) {
        var definition = new ItemDefinition<>(englishName, CrazyAddons.makeId(id), factory.apply(new Item.Properties()));
        ITEMS.add(definition);
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> part(
            String englishName, String id, Function<Item.Properties, T> factory, String recipe, Map<String, Item> recipe_map) {
        var def = item(englishName, id, factory, recipe, recipe_map);
        PARTS.add(def);
        return def;
    }

    public static <T extends Item> ItemDefinition<T> logicCard(
            String englishName, String id, Function<Item.Properties, T> factory, String recipe, Map<String, Item> recipe_map) {
        var def = item(englishName, id, factory, recipe, recipe_map);
        CARDS.add(def);
        return def;
    }
}