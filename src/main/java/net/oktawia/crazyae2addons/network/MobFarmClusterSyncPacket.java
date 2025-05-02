package net.oktawia.crazyae2addons.network;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.entities.MobFarmBE;

import java.util.function.Supplier;

public class MobFarmClusterSyncPacket {
    private final BlockPos min;
    private final BlockPos max;
    private final CompoundTag packetTag;
    private final BlockPos coreBlock;

    public MobFarmClusterSyncPacket(BlockPos min, BlockPos max, CompoundTag packetTag, BlockPos coreBlock) {
        this.min = min;
        this.max = max;
        this.packetTag = packetTag;
        this.coreBlock = coreBlock;
    }

    public static void encode(MobFarmClusterSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.min);
        buf.writeBlockPos(packet.max);
        buf.writeNbt(packet.packetTag);
        buf.writeBlockPos(packet.coreBlock);
    }

    public static MobFarmClusterSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos min = buf.readBlockPos();
        BlockPos max = buf.readBlockPos();
        CompoundTag tag = buf.readNbt();
        BlockPos core = buf.readBlockPos();
        return new MobFarmClusterSyncPacket(min, max, tag, core);
    }

    public static void handle(MobFarmClusterSyncPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            MobFarmCluster cluster = new MobFarmCluster(packet.min, packet.max);
            if (level.getBlockEntity(packet.min) instanceof MobFarmBE mfb && mfb.getCluster() != null){
                cluster = mfb.getCluster();
            }
            cluster.setLevel(level);
            cluster.readFromNBT(packet.packetTag);
            cluster.readBlockEntitiesFromNBT(level, packet.packetTag);
            for (BlockPos pos : BlockPos.betweenClosed(packet.min, packet.max)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MobFarmBE mfb) {
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