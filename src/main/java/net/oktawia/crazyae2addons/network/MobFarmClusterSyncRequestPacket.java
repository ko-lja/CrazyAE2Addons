package net.oktawia.crazyae2addons.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.entities.MobFarmBE;

import java.util.function.Supplier;

public class MobFarmClusterSyncRequestPacket {
    private final BlockPos corePos;
    private final Level level;

    public MobFarmClusterSyncRequestPacket(BlockPos corePos, Level level) {
        this.corePos = corePos;
        this.level = level;
    }

    public static void encode(MobFarmClusterSyncRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.corePos);
        buf.writeUtf(pkt.level.dimension().location().toString());
    }

    public static ServerLevel getLevelFromString(String str) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (str.isEmpty()) return null;
        ResourceLocation rl = new ResourceLocation(str);
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl);
        return server.getLevel(key);
    }

    public static MobFarmClusterSyncRequestPacket decode(FriendlyByteBuf buf) {
        return new MobFarmClusterSyncRequestPacket(buf.readBlockPos(), getLevelFromString(buf.readUtf()));
    }

    public static void handle(MobFarmClusterSyncRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            BlockEntity be = pkt.level.getBlockEntity(pkt.corePos);
            if (!(be instanceof MobFarmBE mobFarmBE)) return;

            MobFarmCluster cluster = mobFarmBE.getCluster();
            if (cluster == null) return;

            CompoundTag packetTag = new CompoundTag();
            cluster.writeToNBT(packetTag);
            cluster.writeBlockEntitiesToNBT(packetTag);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new MobFarmClusterSyncPacket(
                            cluster.getBoundsMin(),
                            cluster.getBoundsMax(),
                            packetTag,
                            cluster.getCoreBlockEntity().getBlockPos()
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }
}