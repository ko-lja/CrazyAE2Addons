package net.oktawia.crazyae2addons;

import appeng.api.stacks.AEKeyTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.oktawia.crazyae2addons.defs.*;
import net.oktawia.crazyae2addons.defs.regs.*;
import net.oktawia.crazyae2addons.mobstorage.EntityTypeRenderer;
import net.oktawia.crazyae2addons.mobstorage.MobKeyType;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

@Mod(CrazyAddons.MODID)
public class CrazyAddons {
    public static final String MODID = "crazyae2addons";

    public CrazyAddons() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CrazyItemRegistrar.ITEMS.register(modEventBus);

        CrazyBlockRegistrar.BLOCKS.register(modEventBus);
        CrazyBlockRegistrar.BLOCK_ITEMS.register(modEventBus);
        CrazyBlockEntityRegistrar.BLOCK_ENTITIES.register(modEventBus);

        CrazyMenuRegistrar.MENU_TYPES.register(modEventBus);

        modEventBus.addListener((RegisterEvent event) -> {
            if (!event.getRegistryKey().equals(Registries.BLOCK)) {
                return;
            }
            AEKeyTypes.register(MobKeyType.TYPE);
        });

        modEventBus.addListener(this::registerCreativeTab);

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.registerPackets();

        modEventBus.addListener(this::commonSetup);
    }

    public static @NotNull ResourceLocation makeId(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void registerCreativeTab(final RegisterEvent evt) {
        if (evt.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            evt.register(Registries.CREATIVE_MODE_TAB,
                CrazyCreativeTabRegistrar.ID,
                () -> CrazyCreativeTabRegistrar.TAB);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            new UpgradeCards(event);
            MobKeyType.registerContainerItemStrategies();
            CrazyBlockEntityRegistrar.setupBlockEntityTypes();
        });
    }

    @Mod.EventBusSubscriber(
            modid = CrazyAddons.MODID,
            bus   = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityTypeRenderer.initialize();
            Screens.register();
        }
        @SubscribeEvent
        public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders evt) {
            CrazyItemRegistrar.registerPartModels();
        }
    }
}