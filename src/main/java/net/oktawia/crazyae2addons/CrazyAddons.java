package net.oktawia.crazyae2addons;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
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
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.screens.*;
import org.jetbrains.annotations.NotNull;
import appeng.init.client.InitScreens;

@Mod(CrazyAddons.MODID)
public class CrazyAddons {
    public static final String MODID = "crazyae2addons";

    public CrazyAddons(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterEvent);

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.registerPackets();
    }


    public static @NotNull ResourceLocation makeId(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            Blocks.getBlocks().forEach(b -> {
                ForgeRegistries.BLOCKS.register(b.id(), b.block());
                ForgeRegistries.ITEMS.register(b.id(), b.asItem());
            });
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            Items.getItems().forEach(i -> ForgeRegistries.ITEMS.register(i.id(), i.asItem()));
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            BlockEntities.getBlockEntityTypes().forEach(ForgeRegistries.BLOCK_ENTITY_TYPES::register);
        } else if (event.getRegistryKey().equals(Registries.MENU)) {
            Menus.getMenuTypes().forEach(ForgeRegistries.MENU_TYPES::register);
        } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CreativeTab.ID, CreativeTab.TAB);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        new UpgradeCards(event);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            Screens.register();
        }
    }
}