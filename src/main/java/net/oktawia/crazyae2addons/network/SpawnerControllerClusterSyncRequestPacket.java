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
import net.oktawia.crazyae2addons.clusters.SpawnerControllerCluster;
import net.oktawia.crazyae2addons.entities.MobFarmBE;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;

import java.util.function.Supplier;

public class SpawnerControllerClusterSyncRequestPacket {
    private final BlockPos corePos;
    private final Level level;

    public SpawnerControllerClusterSyncRequestPacket(BlockPos corePos, Level level) {
        this.corePos = corePos;
        this.level = level;
    }

    public static void encode(SpawnerControllerClusterSyncRequestPacket pkt, FriendlyByteBuf buf) {
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

    public static SpawnerControllerClusterSyncRequestPacket decode(FriendlyByteBuf buf) {
        return new SpawnerControllerClusterSyncRequestPacket(buf.readBlockPos(), getLevelFromString(buf.readUtf()));
    }

    public static void handle(SpawnerControllerClusterSyncRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            BlockEntity be = pkt.level.getBlockEntity(pkt.corePos);
            if (!(be instanceof SpawnerControllerBE spawnerBE)) return;

            SpawnerControllerCluster cluster = spawnerBE.getCluster();
            if (cluster == null) return;

            CompoundTag packetTag = new CompoundTag();
            cluster.getInventory().writeToNBT(packetTag, "clusterinventory");
            cluster.getUpgrades().writeToNBT(packetTag, "clusterupgrades");
            cluster.writeBlockEntitiesToNBT(packetTag);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new SpawnerControllerClusterSyncPacket(
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