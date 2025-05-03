package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.clusters.SpawnerControllerCluster;
import net.oktawia.crazyae2addons.entities.MobFarmBE;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;

import java.util.function.Supplier;

public class SpawnerControllerClusterSyncPacket {
    private final BlockPos min;
    private final BlockPos max;
    private final CompoundTag packetTag;
    private final BlockPos coreBlock;

    public SpawnerControllerClusterSyncPacket(BlockPos min, BlockPos max, CompoundTag packetTag, BlockPos coreBlock) {
        this.min = min;
        this.max = max;
        this.packetTag = packetTag;
        this.coreBlock = coreBlock;
    }

    public static void encode(SpawnerControllerClusterSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.min);
        buf.writeBlockPos(packet.max);
        buf.writeNbt(packet.packetTag);
        buf.writeBlockPos(packet.coreBlock);
    }

    public static SpawnerControllerClusterSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos min = buf.readBlockPos();
        BlockPos max = buf.readBlockPos();
        CompoundTag tag = buf.readNbt();
        BlockPos core = buf.readBlockPos();
        return new SpawnerControllerClusterSyncPacket(min, max, tag, core);
    }

    public static void handle(SpawnerControllerClusterSyncPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            SpawnerControllerCluster cluster = new SpawnerControllerCluster(packet.min, packet.max);
            if (level.getBlockEntity(packet.min) instanceof SpawnerControllerBE mfb && mfb.getCluster() != null){
                cluster = mfb.getCluster();
            }
            cluster.setLevel(level);
            cluster.readFromNBT(packet.packetTag);
            cluster.readBlockEntitiesFromNBT(level, packet.packetTag);
            for (BlockPos pos : BlockPos.betweenClosed(packet.min, packet.max)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SpawnerControllerBE mfb) {
                    mfb.setCluster(cluster);
                    if (pos.equals(packet.coreBlock)) {
                        cluster.setCoreBlockEntity(mfb);
                    }
                }
            }
            cluster.done();
        });
        ctx.setPacketHandled(true);
    }
}