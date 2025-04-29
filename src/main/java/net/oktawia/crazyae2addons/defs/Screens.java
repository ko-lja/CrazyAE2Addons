package net.oktawia.crazyae2addons.defs;

import appeng.init.client.InitScreens;
import net.oktawia.crazyae2addons.IsModLoaded;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.*;
import net.oktawia.crazyae2addons.mobstorage.MobExportBusMenu;
import net.oktawia.crazyae2addons.mobstorage.MobExportBusScreen;
import net.oktawia.crazyae2addons.screens.*;

public final class Screens {

    public static void register() {

        InitScreens.register(CrazyMenuRegistrar.CRAFTING_CANCELER_MENU.get(),
                CraftingCancelerScreen<CraftingCancelerMenu>::new,
                "/screens/crafting_canceler.json");

        InitScreens.register(CrazyMenuRegistrar.ENTITY_TICKER_MENU.get(),
                EntityTickerScreen<EntityTickerMenu>::new,
                "/screens/entity_ticker.json");

        InitScreens.register(CrazyMenuRegistrar.NBT_EXPORT_BUS_MENU.get(),
                NBTExportBusScreen<NBTExportBusMenu>::new,
                "/screens/nbt_export_bus.json");

        InitScreens.register(CrazyMenuRegistrar.CRAZY_PATTERN_MODIFIER_MENU.get(),
                CrazyPatternModifierScreen<CrazyPatternModifierMenu>::new,
                "/screens/crazy_pattern_modifier.json");

        InitScreens.register(CrazyMenuRegistrar.DISPLAY_MENU.get(),
                DisplayScreen<DisplayMenu>::new,
                "/screens/display.json");

        InitScreens.register(CrazyMenuRegistrar.ME_DATA_CONTROLLER_MENU.get(),
                MEDataControllerScreen<MEDataControllerMenu>::new,
                "/screens/me_data_controller.json");

        InitScreens.register(CrazyMenuRegistrar.DATA_EXTRACTOR_MENU.get(),
                DataExtractorScreen<DataExtractorMenu>::new,
                "/screens/data_extractor.json");

        InitScreens.register(CrazyMenuRegistrar.DATA_PROCESSOR_MENU.get(),
                DataProcessorScreen<DataProcessorMenu>::new,
                "/screens/data_processor.json");

        InitScreens.register(CrazyMenuRegistrar.DATA_PROCESSOR_SUB_MENU.get(),
                DataProcessorSubScreen<DataProcessorSubMenu>::new,
                "/screens/data_processor_sub.json");

        InitScreens.register(CrazyMenuRegistrar.DATA_TRACKER_MENU.get(),
                DataTrackerScreen<DataTrackerMenu>::new,
                "/screens/data_tracker.json");

        InitScreens.register(CrazyMenuRegistrar.CHUNKY_FLUID_P2P_TUNNEL_MENU.get(),
                ChunkyFluidP2PTunnelScreen<ChunkyFluidP2PTunnelMenu>::new,
                "/screens/chunky_fluid_p2p_tunnel.json");

        InitScreens.register(CrazyMenuRegistrar.ENERGY_EXPORTER_MENU.get(),
                EnergyExporterScreen<EnergyExporterMenu>::new,
                "/screens/energy_exporter.json");

        InitScreens.register(CrazyMenuRegistrar.RIGHT_CLICK_PROVIDER_MENU.get(),
                RightClickProviderScreen<RightClickProviderMenu>::new,
                "/screens/right_click_provider.json");

        InitScreens.register(CrazyMenuRegistrar.AMPERE_METER_MENU.get(),
                AmpereMeterScreen<AmpereMeterMenu>::new,
                "/screens/ampere_meter.json");

        InitScreens.register(CrazyMenuRegistrar.ISOLATED_DATA_PROCESSOR_MENU.get(),
                IsolatedDataProcessorScreen<IsolatedDataProcessorMenu>::new,
                "/screens/data_processor.json");

        InitScreens.register(CrazyMenuRegistrar.ISOLATED_DATA_PROCESSOR_SUB_MENU.get(),
                IsolatedDataProcessorSubScreen<IsolatedDataProcessorSubMenu>::new,
                "/screens/data_processor_sub.json");

        InitScreens.register(CrazyMenuRegistrar.CRAZY_PATTERN_MULTIPLIER_MENU.get(),
                CrazyPatternMultiplierScreen<CrazyPatternMultiplierMenu>::new,
                "/screens/crazy_pattern_multiplier.json");

        InitScreens.register(CrazyMenuRegistrar.IMPULSED_PATTERN_PROVIDER_MENU.get(),
                ImpulsedPatternProviderScreen<ImpulsedPatternProviderMenu>::new,
                "/screens/impulsed_pattern_provider.json");

        InitScreens.register(CrazyMenuRegistrar.SIGNALLING_INTERFACE_MENU.get(),
                SignallingInterfaceScreen<SignallingInterfaceMenu>::new,
                "/screens/signalling_interface.json");

        InitScreens.register(CrazyMenuRegistrar.SET_STOCK_AMOUNT_MENU.get(),
                SetStockAmountScreen<SetStockAmountMenu>::new,
                "/screens/set_stock_amount.json");

        InitScreens.register(CrazyMenuRegistrar.MOB_EXPORT_BUS_MENU.get(),
                MobExportBusScreen<MobExportBusMenu>::new,
                "/screens/mob_export_bus.json");

        InitScreens.register(
                CrazyMenuRegistrar.MOB_FARM_CONTROLLER_MENU.get(),
                MobFarmControllerScreen<MobFarmControllerMenu>::new,
                "/screens/mob_farm_controller.json"
        );

        if (IsModLoaded.isGTCEuLoaded() && CrazyMenuRegistrar.CIRCUITED_PATTERN_PROVIDER_MENU != null) {
            InitScreens.register(CrazyMenuRegistrar.CIRCUITED_PATTERN_PROVIDER_MENU.get(),
                    CircuitedPatternProviderScreen<CircuitedPatternProviderMenu>::new,
                    "/screens/circuited_pattern_provider.json");
        }
    }

    private Screens() {}
}