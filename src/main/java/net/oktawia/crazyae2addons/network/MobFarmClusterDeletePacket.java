package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.entities.MobFarmWallBE;

import java.util.function.Supplier;

public class MobFarmClusterDeletePacket {
    private final BlockPos min;
    private final BlockPos max;
    private final CompoundTag packetTag;

    public MobFarmClusterDeletePacket(BlockPos min, BlockPos max, CompoundTag packetTag) {
        this.min = min;
        this.max = max;
        this.packetTag = packetTag;
    }

    public static void encode(MobFarmClusterDeletePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.min);
        buf.writeBlockPos(packet.max);
        buf.writeNbt(packet.packetTag);
    }

    public static MobFarmClusterDeletePacket decode(FriendlyByteBuf buf) {
        return new MobFarmClusterDeletePacket(
                buf.readBlockPos(),
                buf.readBlockPos(),
                buf.readNbt()
        );
    }

    public static void handle(MobFarmClusterDeletePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            MobFarmCluster cluster = new MobFarmCluster(packet.min, packet.max);
            cluster.inventory.readFromNBT(packet.packetTag, "clusterinventory");
            cluster.upgrades.readFromNBT(packet.packetTag, "clusterupgrades");
            for (BlockPos pos : BlockPos.betweenClosed(packet.min, packet.max)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MobFarmWallBE wall) {
                    wall.setCluster(cluster);
                }
            }
            cluster.done();
        });
        ctx.setPacketHandled(true);
    }
}
