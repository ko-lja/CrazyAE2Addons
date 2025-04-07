package net.oktawia.crazyae2addons.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.oktawia.crazyae2addons.CrazyAddons;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CrazyAddons.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                DisplayValuePacket.class,
                DisplayValuePacket::encode,
                DisplayValuePacket::decode,
                DisplayValuePacket::handle
        );
        INSTANCE.registerMessage(
                id++,
                DataValuesPacket.class,
                DataValuesPacket::encode,
                DataValuesPacket::decode,
                DataValuesPacket::handle
        );
    }
}
