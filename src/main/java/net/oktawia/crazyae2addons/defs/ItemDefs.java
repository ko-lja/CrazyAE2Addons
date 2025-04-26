package net.oktawia.crazyae2addons.defs;

import appeng.core.definitions.*;
import appeng.items.parts.PartItem;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.compat.GregTech.GTEnergyExporterPart;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.parts.*;
import net.oktawia.crazyae2addons.items.*;
import net.oktawia.crazyae2addons.compat.GregTech.*;

import java.util.*;

public class ItemDefs {

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();
    private static final List<ItemDefinition<?>> CARDS = new ArrayList<>();
    private static final List<ItemDefinition<?>> PARTS = new ArrayList<>();
    private static final Map<Item, Map.Entry<String, Map<String, Item>>> ITEM_RECIPES = new HashMap<>();

    public static List<ItemDefinition<?>> getCards() {
        return Collections.unmodifiableList(CARDS);
    }
    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }
    public static List<ItemDefinition<?>> getParts() {
        return Collections.unmodifiableList(PARTS);
    }
    public static Map<Item, Map.Entry<String, Map<String, Item>>> getItemRecipes() {
        return ITEM_RECIPES;
    }

    public static ItemDefinition<CircuitUpgradeCard> item(
            Item item,
            String recipe,
            Map<String, Item> recipeMap
    ) {
        ITEM_RECIPES.put(item, Map.entry(recipe, recipeMap));
        return null;
    }

    public static void registerRecipes(){
        if (ModList.get().isLoaded("gtceu")){
            item(
                CrazyItemRegistrar.CIRCUIT_UPGRADE_CARD_ITEM.get(),
                "CT",
                Map.of(
                    "C", AEItems.ADVANCED_CARD.asItem(),
                    "T", AEItems.LOGIC_PROCESSOR_PRESS.asItem()
                )
            );
        }

        item(
            CrazyItemRegistrar.LOGIC_CARD.get(),
            "AS",
            Map.of(
                "A", AEItems.ADVANCED_CARD.asItem(),
                "S", AEItems.SKY_DUST.asItem()
            )
        );

        item(
            CrazyItemRegistrar.ADD_CARD.get(),
            "DC",
            Map.of(
                "D", AEItems.SKY_DUST.asItem(),
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.SUB_CARD.get(),
            "CD",
            Map.of(
                "D", AEItems.SKY_DUST.asItem(),
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.MUL_CARD.get(),
            "D/C",
            Map.of(
                "D", AEItems.SKY_DUST.asItem(),
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.DIV_CARD.get(),
            "C/D",
            Map.of(
                "D", AEItems.SKY_DUST.asItem(),
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.MAX_CARD.get(),
            "DC",
            Map.of(
                "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.MIN_CARD.get(),
            "CD",
            Map.of(
                "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.BSR_CARD.get(),
            "D/C",
            Map.of(
                "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.BSL_CARD.get(),
            "C/D",
            Map.of(
                "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                "C", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.HIT_CARD.get(),
            "DCR",
            Map.of(
                "D", net.minecraft.world.item.Items.GLOWSTONE_DUST,
                "C", CrazyItemRegistrar.LOGIC_CARD.get(),
                "R", net.minecraft.world.item.Items.REDSTONE
            )
        );

        item(
            CrazyItemRegistrar.HIF_CARD.get(),
            "DCR",
            Map.of(
                "D", AEItems.SKY_DUST.asItem(),
                "C", CrazyItemRegistrar.LOGIC_CARD.get(),
                "R", net.minecraft.world.item.Items.REDSTONE
            )
        );

        item(
            CrazyItemRegistrar.RR_ITEM_P2P_TUNNEL_PART.get(),
            "PE",
            Map.of(
                "P", AEParts.ITEM_P2P_TUNNEL.asItem(),
                "E", AEItems.EQUAL_DISTRIBUTION_CARD.asItem()
            )
        );

        item(
            CrazyItemRegistrar.NBT_EXPORT_BUS_PART_ITEM.get(),
            "ET/TL",
            Map.of(
                "E", AEParts.EXPORT_BUS.asItem(),
                "T", net.minecraft.world.item.Items.NAME_TAG,
                "L", AEItems.LOGIC_PROCESSOR.asItem()
            )
        );

        item(
            CrazyItemRegistrar.DISPLAY_MONITOR_PART_ITEM.get(),
            "TL",
            Map.of(
                "T", AEParts.MONITOR.asItem(),
                "L", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.DATA_EXTRACTOR_PART_ITEM.get(),
            "IL",
            Map.of(
                "I", AEParts.IMPORT_BUS.asItem(),
                "L", CrazyItemRegistrar.LOGIC_CARD.get()
            )
        );

        item(
            CrazyItemRegistrar.CHUNKY_FLUID_P2P_TUNNEL_PART.get(),
            "TL",
            Map.of(
                "T", AEParts.FLUID_P2P_TUNNEL.asItem(),
                "L", AEItems.LOGIC_PROCESSOR.asItem()
            )
        );

        item(
            CrazyItemRegistrar.ENERGY_EXPORTER_PART_ITEM.get(),
            "ERR",
            Map.of(
                "E", AEParts.EXPORT_BUS.asItem(),
                "R", net.minecraft.world.item.Items.REDSTONE
            )
        );

        item(
            CrazyItemRegistrar.ENTITY_TICKER_PART_ITEM.get(),
            "DND/NEN/DND",
            Map.of(
                "D", net.minecraft.world.item.Items.DIAMOND,
                "N", net.minecraft.world.item.Items.NETHER_STAR,
                "E", CrazyItemRegistrar.ENERGY_EXPORTER_PART_ITEM.get()
            )
        );

        item(
            CrazyItemRegistrar.RIGHT_CLICK_PROVIDER_PART_ITEM.get(),
            "ED/TP",
            Map.of(
                "E", AEParts.EXPORT_BUS.asItem(),
                "D", net.minecraft.world.item.Items.DIAMOND,
                "T", net.minecraft.world.item.Items.NAME_TAG,
                "P", AEItems.ENGINEERING_PROCESSOR.asItem()
            )
        );

        item(
            CrazyItemRegistrar.CRAZY_PATTERN_MODIFIER_ITEM.get(),
            "PZ/ZP",
            Map.of(
                "P", AEItems.BLANK_PATTERN.asItem(),
                "Z", AEItems.LOGIC_PROCESSOR.asItem()
            )
        );

        item(
            CrazyItemRegistrar.CRAZY_PATTERN_MULTIPLIER_ITEM.get(),
            "PZ/ZP",
            Map.of(
                "P", AEItems.BLANK_PATTERN.asItem(),
                "Z", AEItems.CALCULATION_PROCESSOR.asItem()
            )
        );

    }
}