package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.entities.MobFarmBE;

import java.util.function.Supplier;

public class MobFarmClusterDeletePacket {
    private final BlockPos pos;

    public MobFarmClusterDeletePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MobFarmClusterDeletePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static MobFarmClusterDeletePacket decode(FriendlyByteBuf buf) {
        return new MobFarmClusterDeletePacket(
                buf.readBlockPos()
        );
    }

    public static void handle(MobFarmClusterDeletePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if(level != null && level.isClientSide()){
                if (level.getBlockEntity(packet.pos) instanceof MobFarmBE wall) {
                    wall.setCluster(null);
                    wall.saveChanges();
                }
            }
        });
        ctxSupplier.get().setPacketHandled(true);
    }
}
