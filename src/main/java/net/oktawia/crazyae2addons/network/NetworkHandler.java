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
                UpdatePatternsPacket.class,
                UpdatePatternsPacket::encode,
                UpdatePatternsPacket::decode,
                UpdatePatternsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                SyncBlockClientPacket.class,
                SyncBlockClientPacket::encode,
                SyncBlockClientPacket::decode,
                SyncBlockClientPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void registerServerPackets() {
    }
}
