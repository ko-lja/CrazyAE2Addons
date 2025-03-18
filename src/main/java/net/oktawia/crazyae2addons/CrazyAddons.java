package net.oktawia.crazyae2addons;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.oktawia.crazyae2addons.defs.*;
import net.oktawia.crazyae2addons.menus.*;
import net.oktawia.crazyae2addons.screens.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import appeng.init.client.InitScreens;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrazyAddons.MODID)
public class CrazyAddons
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "crazyae2addons";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static String checkmark = "✔";
    public static String xmark = "✖";

    public CrazyAddons(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                Blocks.getBlocks().forEach(b -> {
                    ForgeRegistries.BLOCKS.register(b.id(), b.block());
                    ForgeRegistries.ITEMS.register(b.id(), b.asItem());
                });
            }
            if (event.getRegistryKey().equals(Registries.ITEM)) {
                Items.getItems().forEach(i -> ForgeRegistries.ITEMS.register(i.id(), i.asItem()));
            }
            if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
                BlockEntities.getBlockEntityTypes().forEach(ForgeRegistries.BLOCK_ENTITY_TYPES::register);
            }
            if (event.getRegistryKey().equals(Registries.MENU)) {
                Menus.getMenuTypes().forEach(ForgeRegistries.MENU_TYPES::register);
            }
            if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
                Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CreativeTab.ID, CreativeTab.TAB);
            }
        });
    }

    public static @NotNull ResourceLocation makeId(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event){
        new UpgradeCards(event);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
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
                    "/screens/pattern_modifier.json"
            );
            InitScreens.register(
                    Menus.AUTO_ENCHANTER_MENU,
                    AutoEnchanterScreen<AutoEnchanterMenu>::new,
                    "/screens/auto_enchanter.json"
            );
        }
    }
}
