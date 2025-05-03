package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;

import java.util.function.Supplier;

public class SpawnerControllerClusterDeletePacket {
    private final BlockPos pos;

    public SpawnerControllerClusterDeletePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SpawnerControllerClusterDeletePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static SpawnerControllerClusterDeletePacket decode(FriendlyByteBuf buf) {
        return new SpawnerControllerClusterDeletePacket(
                buf.readBlockPos()
        );
    }

    public static void handle(SpawnerControllerClusterDeletePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if(level != null && level.isClientSide()){
                if (level.getBlockEntity(packet.pos) instanceof SpawnerControllerBE wall) {
                    wall.setCluster(null);
                    wall.saveChanges();
                }
            }
        });
        ctxSupplier.get().setPacketHandled(true);
    }
}
