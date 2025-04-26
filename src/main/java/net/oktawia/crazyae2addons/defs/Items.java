package net.oktawia.crazyae2addons.defs;

import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.core.definitions.*;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.compat.GregTech.GTEnergyExporterPart;
import net.oktawia.crazyae2addons.parts.*;
import net.oktawia.crazyae2addons.items.*;
import net.oktawia.crazyae2addons.compat.GregTech.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Items {

    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, CrazyAddons.MODID);
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
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
                    () -> Map.of(
                            "D", AEItems.SKY_DUST.asItem(),
                            "C", LOGIC_CARD.asItem(),
                            "R", net.minecraft.world.item.Items.REDSTONE
                    )
            ));

    public static final ItemDefinition<RRItemP2PTunnelPartItem> RR_ITEM_P2P_TUNNEL_PART = Util.make(() -> part(
            RRItemP2PTunnelPart.class,
            "Round Robin Item P2P Tunnel",
            "rr_item_p2p_tunnel",
            RRItemP2PTunnelPartItem::new,
            "PE",
            () -> Map.of(
                    "P", AEParts.ITEM_P2P_TUNNEL.asItem(),
                    "E", AEItems.EQUAL_DISTRIBUTION_CARD.asItem()
            )
    ));

    public static final ItemDefinition<NBTExportBusPartItem> NBT_EXPORT_BUS_PART_ITEM = Util.make(() -> part(
            NBTExportBusPart.class,
            "NBT Export Bus",
            "nbt_export_bus",
            NBTExportBusPartItem::new,
            "ET/TL",
            () -> Map.of(
                    "E", AEParts.EXPORT_BUS.asItem(),
                    "T", net.minecraft.world.item.Items.NAME_TAG,
                    "L", AEItems.LOGIC_PROCESSOR.asItem()
            )
    ));

    public static final ItemDefinition<DisplayPartItem> DISPLAY_MONITOR_PART_ITEM = Util.make(() -> part(
            DisplayPart.class,
            "Display Monitor",
            "display_monitor",
            DisplayPartItem::new,
            "TL",
            () -> Map.of(
                    "T", AEParts.MONITOR.asItem(),
                    "L", LOGIC_CARD.asItem()
            )
    ));

    public static final ItemDefinition<PartItem<? extends DataExtractorPart>> DATA_EXTRACTOR_PART_ITEM = Util.make(() -> part(
            IsModLoaded.isGTCEuLoaded()
                ? GTDataExtractorPart.class
                : DataExtractorPart.class,
            "Data Extractor",
            "data_extractor",
            IsModLoaded.isGTCEuLoaded()
                    ? GTDataExtractorPartItem::new
                    : DataExtractorPartItem::new,
            "IL",
            () -> Map.of(
                    "I", AEParts.IMPORT_BUS.asItem(),
                    "L", LOGIC_CARD.asItem()
            )
    ));

    public static final ItemDefinition<ChunkyFluidP2PTunnelPartItem> CHUNKY_FLUID_P2P_TUNNEL_PART = Util.make(() -> part(
            ChunkyFluidP2PTunnelPart.class,
            "Chunky Fluid P2P Tunnel",
            "chunky_fluid_p2p_tunnel",
            ChunkyFluidP2PTunnelPartItem::new,
            "TL",
            () -> Map.of(
                    "T", AEParts.FLUID_P2P_TUNNEL.asItem(),
                    "L", AEItems.LOGIC_PROCESSOR.asItem()
            )
    ));

    public static final ItemDefinition<PartItem<? extends EnergyExporterPart>> ENERGY_EXPORTER_PART_ITEM = Util.make(() -> part(
            IsModLoaded.isGTCEuLoaded()
                ? GTEnergyExporterPart.class
                : EnergyExporterPart.class,
            "Energy Exporter",
            "energy_exporter",
            IsModLoaded.isGTCEuLoaded()
                    ? GTEnergyExporterPartItem::new
                    : EnergyExporterPartItem::new,
            "ERR",
            () -> Map.of(
                    "E", AEParts.EXPORT_BUS.asItem(),
                    "R", net.minecraft.world.item.Items.REDSTONE
            )
    ));

    public static final ItemDefinition<EntityTickerPartItem> ENTITY_TICKER_PART_ITEM = Util.make(() -> part(
            EntityTickerPart.class,
            "Entity Ticker",
            "entity_ticker",
            EntityTickerPartItem::new,
            "DND/NEN/DND",
            () -> Map.of(
                    "D", net.minecraft.world.item.Items.DIAMOND,
                    "N", net.minecraft.world.item.Items.NETHER_STAR,
                    "E", ENERGY_EXPORTER_PART_ITEM.asItem()
            )
    ));

    public static final ItemDefinition<RightClickProviderPartItem> RIGHT_CLICK_PROVIDER_PART_ITEM = Util.make(() -> part(
            RightClickProviderPart.class,
            "Right Click Provider",
            "rc_provider",
            RightClickProviderPartItem::new,
            "ED/TP",
            () -> Map.of(
                    "E", AEParts.EXPORT_BUS.asItem(),
                    "D", net.minecraft.world.item.Items.DIAMOND,
                    "T", net.minecraft.world.item.Items.NAME_TAG,
                    "P", AEItems.ENGINEERING_PROCESSOR.asItem()
            )
    ));

    public static final ItemDefinition<CrazyPatternModifierItem> CRAZY_PATTERN_MODIFIER_ITEM = Util.make(() ->
            item(
            "Crazy Pattern Modifier",
                    "crazy_pattern_modifier",
                    CrazyPatternModifierItem::new,
                    "PZ/ZP",
                    () -> Map.of(
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
                    () -> Map.of(
                            "P", AEItems.BLANK_PATTERN.asItem(),
                            "Z", AEItems.CALCULATION_PROCESSOR.asItem()
                    )
            ));

    public static final ItemDefinition<CircuitUpgradeCard> CIRCUIT_UPGRADE_CARD_ITEM =
            ModList.get().isLoaded("gtceu") ? Util.make(() ->
            item(
            "Circuit Upgrade Card",
                    "circuit_upgrade_card",
                    CircuitUpgradeCard::new,
                    "CT",
                    () -> Map.of(
                            "C", AEItems.ADVANCED_CARD.asItem(),
                            "T", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
                    )
            )) : null;

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
            String englishName,
            String id,
            Function<Item.Properties, T> factory,
            String recipe,
            Supplier<Map<String, Item>> recipeMapSupplier
    ) {
        T itemInstance = factory.apply(new Item.Properties());
        ITEM_REGISTER.register(id, () -> itemInstance);
        var definition = new ItemDefinition<>(englishName, CrazyAddons.makeId(id), itemInstance);
        ITEMS.add(definition);
        ITEM_RECIPES.put(definition, Map.entry(recipe, recipeMapSupplier.get()));
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> item(
            String englishName,
            String id,
            Function<Item.Properties, T> factory
    ) {
        T itemInstance = factory.apply(new Item.Properties());
        ITEM_REGISTER.register(id, () -> itemInstance);
        var definition = new ItemDefinition<>(englishName, CrazyAddons.makeId(id), itemInstance);
        ITEMS.add(definition);
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> part(
            Class<? extends IPart> partClass,
            String englishName,
            String id,
            Function<Item.Properties, T> factory,
            String recipe,
            Supplier<Map<String, Item>> recipeMapSupplier
    ) {
        var def = item(englishName, id, factory, recipe, recipeMapSupplier);
        PARTS.add(def);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            PartModels.registerModels(
                    PartModelsHelper.createModels(partClass)
            );
        });
        return def;
    }

    public static <T extends Item> ItemDefinition<T> logicCard(
            String englishName, String id, Function<Item.Properties, T> factory, String recipe, Supplier<Map<String, Item>> recipe_map) {
        var def = item(englishName, id, factory, recipe, recipe_map);
        CARDS.add(def);
        return def;
    }
}