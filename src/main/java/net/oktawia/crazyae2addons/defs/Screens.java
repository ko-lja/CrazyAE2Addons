package net.oktawia.crazyae2addons.defs;

import appeng.init.client.InitScreens;
import net.oktawia.crazyae2addons.menus.*;
import net.oktawia.crazyae2addons.parts.CircuitedPatternProviderPart;
import net.oktawia.crazyae2addons.screens.*;

public class Screens {
    public static void register() {
        InitScreens.register(
                Menus.CRAFTING_CANCELER_MENU,
                CraftingCancelerScreen<CraftingCancelerMenu>::new,
                "/screens/crafting_canceler.json"
        );
        InitScreens.register(
                Menus.ENTITY_TICKER_MENU,
                EntityTickerScreen<EntityTickerMenu>::new,
                "/screens/entity_ticker.json"
        );
        InitScreens.register(
                Menus.NBT_EXPORT_BUS_MENU,
                NBTExportBusScreen<NBTExportBusMenu>::new,
                "/screens/nbt_export_bus.json"
        );
        InitScreens.register(
                Menus.PATTERN_MODIFIER_MENU,
                PatternModifierScreen<PatternModifierMenu>::new,
                "/screens/crazy_pattern_modifier.json"
        );
        InitScreens.register(
                Menus.AUTO_ENCHANTER_MENU,
                AutoEnchanterScreen<AutoEnchanterMenu>::new,
                "/screens/auto_enchanter.json"
        );
        InitScreens.register(
                Menus.DISPLAY_MENU,
                DisplayScreen<DisplayMenu>::new,
                "/screens/display.json"
        );
        InitScreens.register(
                Menus.ME_DATA_CONTROLLER_MENU,
                MEDataControllerScreen<MEDataControllerMenu>::new,
                "/screens/me_data_controller.json"
        );
        InitScreens.register(
                Menus.DATA_EXTRACTOR_MENU,
                DataExtractorScreen<DataExtractorMenu>::new,
                "/screens/data_extractor.json"
        );
        InitScreens.register(
                Menus.DATA_PROCESSOR_MENU,
                DataProcessorScreen<DataProcessorMenu>::new,
                "/screens/data_processor.json"
        );
        InitScreens.register(
                Menus.DATA_PROCESSOR_SUB_MENU,
                DataProcessorSubScreen<DataProcessorSubMenu>::new,
                "/screens/data_processor_sub.json"
        );
        InitScreens.register(
                Menus.DATA_TRACKER_MENU,
                DataTrackerScreen<DataTrackerMenu>::new,
                "/screens/data_tracker.json"
        );
        InitScreens.register(
                Menus.CHUNKY_FLUID_P2P_TUNNEL_MENU,
                ChunkyFluidP2PTunnelScreen<ChunkyFluidP2PTunnelMenu>::new,
                "/screens/chunky_fluid_p2p_tunnel.json"
        );
        InitScreens.register(
                Menus.CIRCUITED_PATTERN_PROVIDER_MENU,
                CircuitedPatternProviderScreen<CircuitedPatternProviderMenu>::new,
                "/screens/circuited_pp.json"
        );
        InitScreens.register(
                Menus.CIRCUITED_PATTERN_PROVIDER_PART_MENU,
                CircuitedPatternProviderPartScreen<CircuitedPatternProviderPartMenu>::new,
                "/screens/circuited_pp.json"
        );
        InitScreens.register(
                Menus.ENERGY_EXPORTER_MENU,
                EnergyExporterScreen<EnergyExporterMenu>::new,
                "/screens/energy_exporter.json"
        );
    }
}
