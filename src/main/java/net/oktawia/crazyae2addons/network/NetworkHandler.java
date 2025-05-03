package net.oktawia.crazyae2addons.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.oktawia.crazyae2addons.CrazyAddons;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CrazyAddons.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerClientPackets(){
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                DisplayValuePacket.class,
                DisplayValuePacket::encode,
                DisplayValuePacket::decode,
                DisplayValuePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                DataValuesPacket.class,
                DataValuesPacket::encode,
                DataValuesPacket::decode,
                DataValuesPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                MobFarmClusterSyncPacket.class,
                MobFarmClusterSyncPacket::encode,
                MobFarmClusterSyncPacket::decode,
                MobFarmClusterSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                MobFarmClusterDeletePacket.class,
                MobFarmClusterDeletePacket::encode,
                MobFarmClusterDeletePacket::decode,
                MobFarmClusterDeletePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                SpawnerControllerClusterDeletePacket.class,
                SpawnerControllerClusterDeletePacket::encode,
                SpawnerControllerClusterDeletePacket::decode,
                SpawnerControllerClusterDeletePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                SpawnerControllerClusterSyncPacket.class,
                SpawnerControllerClusterSyncPacket::encode,
                SpawnerControllerClusterSyncPacket::decode,
                SpawnerControllerClusterSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void registerServerPackets() {
        int id = 100;
        INSTANCE.registerMessage(
                id++,
                MobFarmClusterSyncRequestPacket.class,
                MobFarmClusterSyncRequestPacket::encode,
                MobFarmClusterSyncRequestPacket::decode,
                MobFarmClusterSyncRequestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        INSTANCE.registerMessage(
                id++,
                SpawnerControllerClusterSyncRequestPacket.class,
                SpawnerControllerClusterSyncRequestPacket::encode,
                SpawnerControllerClusterSyncRequestPacket::decode,
                SpawnerControllerClusterSyncRequestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
