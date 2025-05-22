package net.oktawia.crazyae2addons.defs.regs;

import appeng.api.parts.PartModels;
import appeng.items.AEBaseItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.items.storage.StorageTier;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.compat.GregTech.*;
import net.oktawia.crazyae2addons.items.*;
import net.oktawia.crazyae2addons.mobstorage.*;
import net.oktawia.crazyae2addons.parts.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CrazyItemRegistrar {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CrazyAddons.MODID);

    private static final List<RegistryObject<? extends AEBaseItem>> CARDS = new ArrayList<>();

    public static List<? extends AEBaseItem> getCards() {
        return CARDS.stream().map(RegistryObject::get).toList();
    }

    public static List<Item> getItems() {
        return ITEMS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .toList();
    }

    public static List<Item> getParts() {
        return ITEMS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(i -> i instanceof PartItem)
                .toList();
    }

    private static <T extends AEBaseItem> RegistryObject<T> regCard(String id, Supplier<T> sup) {
        RegistryObject<T> ro = ITEMS.register(id, sup);
        CARDS.add(ro);
        return ro;
    }

    public static void registerPartModels() {
        for (Item item : getParts()) {
            if (item instanceof PartItem<?> partItem) {
                Class<?> partClass = partItem.getPartClass();
                if (partClass != null) {
                    PartModels.registerModels(PartModelsHelper.createModels(partClass.asSubclass(appeng.api.parts.IPart.class)));
                }
            }
        }
    }

    public static final RegistryObject<LogicCard> LOGIC_CARD = regCard(
            "logic_card", () -> new LogicCard(new Item.Properties()));

    public static final RegistryObject<AddCard> ADD_CARD = regCard(
            "add_card", () -> new AddCard(new Item.Properties()));

    public static final RegistryObject<SubCard> SUB_CARD = regCard(
            "sub_card", () -> new SubCard(new Item.Properties()));

    public static final RegistryObject<MulCard> MUL_CARD = regCard(
            "mul_card", () -> new MulCard(new Item.Properties()));

    public static final RegistryObject<DivCard> DIV_CARD = regCard(
            "div_card", () -> new DivCard(new Item.Properties()));

    public static final RegistryObject<MaxCard> MAX_CARD = regCard(
            "max_card", () -> new MaxCard(new Item.Properties()));

    public static final RegistryObject<MinCard> MIN_CARD = regCard(
            "min_card", () -> new MinCard(new Item.Properties()));

    public static final RegistryObject<BsrCard> BSR_CARD = regCard(
            "bsr_card", () -> new BsrCard(new Item.Properties()));

    public static final RegistryObject<BslCard> BSL_CARD = regCard(
            "bsl_card", () -> new BslCard(new Item.Properties()));

    public static final RegistryObject<HitCard> HIT_CARD = regCard(
            "hit_card", () -> new HitCard(new Item.Properties()));

    public static final RegistryObject<HifCard> HIF_CARD = regCard(
            "hif_card", () -> new HifCard(new Item.Properties()));

    public static final RegistryObject<RRItemP2PTunnelPartItem> RR_ITEM_P2P_TUNNEL_PART =
            ITEMS.register("round_robin_item_p2p_tunnel",
                    () -> new RRItemP2PTunnelPartItem(new Item.Properties()));
    public static final RegistryObject<NBTExportBusPartItem> NBT_EXPORT_BUS_PART_ITEM =
            ITEMS.register("nbt_export_bus",
                    () -> new NBTExportBusPartItem(new Item.Properties()));
    public static final RegistryObject<DisplayPartItem> DISPLAY_MONITOR_PART_ITEM =
            ITEMS.register("display_monitor",
                    () -> new DisplayPartItem(new Item.Properties()));

    public static final RegistryObject<PartItem<? extends DataExtractorPart>> DATA_EXTRACTOR_PART_ITEM =
            ITEMS.register("data_extractor",
                    () -> IsModLoaded.isGTCEuLoaded()
                            ? new GTDataExtractorPartItem(new Item.Properties())
                            : new DataExtractorPartItem(new Item.Properties()));

    public static final RegistryObject<ChunkyFluidP2PTunnelPartItem> CHUNKY_FLUID_P2P_TUNNEL_PART =
            ITEMS.register("chunky_fluid_p2p_tunnel",
                    () -> new ChunkyFluidP2PTunnelPartItem(new Item.Properties()));

    public static final RegistryObject<PartItem<? extends EnergyExporterPart>> ENERGY_EXPORTER_PART_ITEM =
            ITEMS.register("energy_exporter",
                    () -> IsModLoaded.isGTCEuLoaded()
                            ? new GTEnergyExporterPartItem(new Item.Properties())
                            : new EnergyExporterPartItem(new Item.Properties()));

    public static final RegistryObject<CircuitUpgradeCard> CIRCUIT_UPGRADE_CARD_ITEM =
            ITEMS.register("circuit_upgrade_card",
                    () -> new CircuitUpgradeCard(new Item.Properties()));

    public static final RegistryObject<EntityTickerPartItem> ENTITY_TICKER_PART_ITEM =
            ITEMS.register("entity_ticker",
                    () -> new EntityTickerPartItem(new Item.Properties()));

    public static final RegistryObject<RightClickProviderPartItem> RIGHT_CLICK_PROVIDER_PART_ITEM =
            ITEMS.register("right_click_provider",
                    () -> new RightClickProviderPartItem(new Item.Properties()));

    public static final RegistryObject<CrazyPatternModifierItem> CRAZY_PATTERN_MODIFIER_ITEM =
            ITEMS.register("crazy_pattern_modifier",
                    () -> new CrazyPatternModifierItem(new Item.Properties()));
    public static final RegistryObject<CrazyPatternMultiplierItem> CRAZY_PATTERN_MULTIPLIER_ITEM =
            ITEMS.register("crazy_pattern_multiplier",
                    () -> new CrazyPatternMultiplierItem(new Item.Properties()));

    public static final RegistryObject<XpShardItem> XP_SHARD_ITEM =
            ITEMS.register("xp_shard", () -> new XpShardItem(new Item.Properties()));

    public static final RegistryObject<Item> MOB_CELL_HOUSING =
            ITEMS.register("mob_cell_housing",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MOB_CELL_1K = ITEMS.register("mob_storage_cell_1k",
            () -> new MobStorageCell(new Item.Properties().stacksTo(1), StorageTier.SIZE_1K, MOB_CELL_HOUSING.get()));
    public static final RegistryObject<Item> MOB_CELL_4K = ITEMS.register("mob_storage_cell_4k",
            () -> new MobStorageCell(new Item.Properties().stacksTo(1), StorageTier.SIZE_4K, MOB_CELL_HOUSING.get()));
    public static final RegistryObject<Item> MOB_CELL_16K = ITEMS.register("mob_storage_cell_16k",
            () -> new MobStorageCell(new Item.Properties().stacksTo(1), StorageTier.SIZE_16K, MOB_CELL_HOUSING.get()));
    public static final RegistryObject<Item> MOB_CELL_64K = ITEMS.register("mob_storage_cell_64k",
            () -> new MobStorageCell(new Item.Properties().stacksTo(1), StorageTier.SIZE_64K, MOB_CELL_HOUSING.get()));
    public static final RegistryObject<Item> MOB_CELL_256K = ITEMS.register("mob_storage_cell_256k",
            () -> new MobStorageCell(new Item.Properties().stacksTo(1), StorageTier.SIZE_256K,
                    MOB_CELL_HOUSING.get()));

    public static final RegistryObject<MobAnnihilationPlaneItem> MOB_ANNIHILATION_PLANE =
            ITEMS.register("mob_annihilation_plane",
                    () -> new MobAnnihilationPlaneItem(new Item.Properties()));
    public static final RegistryObject<MobExportBusItem> MOB_EXPORT_BUS =
            ITEMS.register("mob_export_bus",
                    () -> new MobExportBusItem(new Item.Properties()));

    public static final RegistryObject<LootingUpgradeCard> LOOTING_UPGRADE_CARD =
            ITEMS.register("looting_upgrade_card",
                    () -> new LootingUpgradeCard(new Item.Properties()));

    public static final RegistryObject<ExperienceUpgradeCard> EXPERIENCE_UPGRADE_CARD =
            ITEMS.register("experience_upgrade_card",
                    () -> new ExperienceUpgradeCard(new Item.Properties()));

    private CrazyItemRegistrar() {}

}