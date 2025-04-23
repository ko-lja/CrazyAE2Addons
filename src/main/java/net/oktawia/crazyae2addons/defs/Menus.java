package net.oktawia.crazyae2addons.defs;

import appeng.core.AppEng;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.oktawia.crazyae2addons.logic.CrazyPatternModifierHost;
import net.oktawia.crazyae2addons.logic.CrazyPatternMultiplierHost;
import net.oktawia.crazyae2addons.parts.*;
import net.oktawia.crazyae2addons.entities.*;
import net.oktawia.crazyae2addons.menus.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Menus {

    private static final Map<ResourceLocation, MenuType<?>> MENU_TYPES = new HashMap<>();

    public static Map<ResourceLocation, MenuType<?>> getMenuTypes() {
        return Collections.unmodifiableMap(MENU_TYPES);
    }

    public static final MenuType<CraftingCancelerMenu> CRAFTING_CANCELER_MENU = create(
            "crafting_canceler",
            CraftingCancelerMenu::new,
            CraftingCancelerBE.class
    );
    public static final MenuType<EntityTickerMenu> ENTITY_TICKER_MENU = create(
            "entity_ticker",
            EntityTickerMenu::new,
            EntityTickerPart.class
    );
    public static final MenuType<NBTExportBusMenu> NBT_EXPORT_BUS_MENU = create(
            "nbt_export_bus",
            NBTExportBusMenu::new,
            NBTExportBusPart.class
    );
    public static final MenuType<CrazyPatternModifierMenu> CRAZY_PATTERN_MODIFIER_MENU = create(
            "crazy_pattern_modifier",
            CrazyPatternModifierMenu::new,
            CrazyPatternModifierHost.class
    );
    public static final MenuType<AutoEnchanterMenu> AUTO_ENCHANTER_MENU = create(
            "auto_enchanter",
            AutoEnchanterMenu::new,
            AutoEnchanterBE.class
    );
    public static final MenuType<DisplayMenu> DISPLAY_MENU = create(
            "display",
            DisplayMenu::new,
            DisplayPart.class
    );
    public static final MenuType<MEDataControllerMenu> ME_DATA_CONTROLLER_MENU = create(
            "me_data_controller",
            MEDataControllerMenu::new,
            MEDataControllerBE.class
    );
    public static final MenuType<DataExtractorMenu> DATA_EXTRACTOR_MENU = create(
            "data_extractor",
            DataExtractorMenu::new,
            DataExtractorPart.class
    );
    public static final MenuType<DataProcessorMenu> DATA_PROCESSOR_MENU = create(
            "data_processor",
            DataProcessorMenu::new,
            DataProcessorBE.class
    );
    public static final MenuType<DataProcessorSubMenu> DATA_PROCESSOR_SUB_MENU = create(
            "data_processor_sub",
            DataProcessorSubMenu::new,
            DataProcessorBE.class
    );
    public static final MenuType<DataTrackerMenu> DATA_TRACKER_MENU = create(
            "data_tracker",
            DataTrackerMenu::new,
            DataTrackerBE.class
    );
    public static final MenuType<ChunkyFluidP2PTunnelMenu> CHUNKY_FLUID_P2P_TUNNEL_MENU = create(
            "chunky_p2p",
            ChunkyFluidP2PTunnelMenu::new,
            ChunkyFluidP2PTunnelPart.class
    );
    public static final MenuType<CircuitedPatternProviderMenu> CIRCUITED_PATTERN_PROVIDER_MENU = create(
            "circuited_pp",
            CircuitedPatternProviderMenu::new,
            CircuitedPatternProviderBE.class
    );
    public static final MenuType<EnergyExporterMenu> ENERGY_EXPORTER_MENU = create(
            "energy_exporter",
            EnergyExporterMenu::new,
            EnergyExporterPart.class
    );
    public static final MenuType<RightClickProviderMenu> RIGHT_CLICK_PROVIDER_MENU = create(
            "rc_provider",
            RightClickProviderMenu::new,
            RightClickProviderPart.class
    );
    public static final MenuType<AmpereMeterMenu> AMPERE_METER_MENU = create(
            "ampere_meter",
            AmpereMeterMenu::new,
            AmpereMeterBE.class
    );
    public static final MenuType<IsolatedDataProcessorMenu> ISOLATED_DATA_PROCESSOR_MENU = create(
            "isolated_data_processor",
            IsolatedDataProcessorMenu::new,
            IsolatedDataProcessorBE.class
    );
    public static final MenuType<IsolatedDataProcessorSubMenu> ISOLATED_DATA_PROCESSOR_SUBMENU = create(
            "isolated_data_processor_sub",
            IsolatedDataProcessorSubMenu::new,
            IsolatedDataProcessorBE.class
    );
    public static final MenuType<CrazyPatternMultiplierMenu> CRAZY_PATTERN_MULTIPLIER_MENU = create(
            "crazy_pattern_multiplier",
            CrazyPatternMultiplierMenu::new,
            CrazyPatternMultiplierHost.class
    );
    public static final MenuType<ImpulsedPatternProviderMenu> IMPULSED_PATTERN_PROVIDER_MENU = create(
            "impulsed_pp",
            ImpulsedPatternProviderMenu::new,
            ImpulsedPatternProviderBE.class
    );
    public static final MenuType<SignallingInterfaceMenu> SIGNALLING_INTERFACE_MENU = create(
            "signalling_interface",
            SignallingInterfaceMenu::new,
            SignallingInterfaceBE.class
    );
    public static final MenuType<SetStockAmountMenu> SET_STOCK_AMOUNT_MENU = create(
            "stock_amount_menu",
            SetStockAmountMenu::new,
            SignallingInterfaceBE.class
    );


    public static <C extends AEBaseMenu, I> MenuType<C> create(
            String id, MenuTypeBuilder.MenuFactory<C, I> factory, Class<I> host) {
        var menu = MenuTypeBuilder.create(factory, host).build(id);
        MENU_TYPES.put(AppEng.makeId(id), menu);
        return menu;
    }
}