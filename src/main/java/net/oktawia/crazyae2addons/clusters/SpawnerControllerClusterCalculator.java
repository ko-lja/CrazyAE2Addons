package net.oktawia.crazyae2addons.clusters;

import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.blocks.SpawnerControllerWall;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SpawnerControllerClusterCalculator extends MBCalculator<SpawnerControllerBE, SpawnerControllerCluster> {

    public static final ClusterPattern STRUCTURE_PATTERN =
            new ClusterPattern(new ResourceLocation("crazyae2addons", "spawner_controller"));

    public SpawnerControllerClusterCalculator(SpawnerControllerBE owner) {
        super(owner);
    }

    @Override
    public boolean checkMultiblockScale(BlockPos min, BlockPos max) {
        return true;
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        BlockPos origin = ClusterPattern.findOrigin(level, target.getBlockPos(), STRUCTURE_PATTERN.getAllValidBlocks());
        for (ClusterPattern.Rotation rotation : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(level, origin, rotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void calculateMultiblock(ServerLevel level, BlockPos loc) {
        if (isModificationInProgress()) return;

        SpawnerControllerCluster current = target.getCluster();
        if (current != null && !current.isDestroyed()) return;

        BlockPos origin = ClusterPattern.findOrigin(level, loc, STRUCTURE_PATTERN.getAllValidBlocks());
        ClusterPattern.Rotation rotation = Arrays.stream(ClusterPattern.Rotation.values())
                .filter(rot -> STRUCTURE_PATTERN.matchesWithRotation(level, origin, rot))
                .findFirst()
                .orElse(null);

        if (rotation == null) {
            target.disconnect(true);
            return;
        }

        boolean doReturn = false;
        SpawnerControllerCluster existing = null;
        for (BlockPos offset : STRUCTURE_PATTERN.getOffsets(rotation)) {
            BlockPos abs = origin.offset(offset);
            BlockEntity be = level.getBlockEntity(abs);
            if (be instanceof SpawnerControllerBE part) {
                existing = part.getCluster();
                if (existing != null && !existing.isDestroyed()) {
                    part.cluster = null;
                    existing.gridNode.destroy();
                    doReturn = true;
                }
            }
        }
        if (doReturn && existing != null) {
            updateBlockEntities(existing, level, existing.getBoundsMin(), existing.getBoundsMax());
            return;
        }

        List<BlockPos> offsets = STRUCTURE_PATTERN.getOffsets(rotation);

        BlockPos min = null;
        BlockPos max = null;
        for (BlockPos offset : offsets) {
            BlockPos abs = origin.offset(offset);
            if (min == null) {
                min = abs;
                max = abs;
            } else {
                min = new BlockPos(
                        Math.min(min.getX(), abs.getX()),
                        Math.min(min.getY(), abs.getY()),
                        Math.min(min.getZ(), abs.getZ())
                );
                max = new BlockPos(
                        Math.max(max.getX(), abs.getX()),
                        Math.max(max.getY(), abs.getY()),
                        Math.max(max.getZ(), abs.getZ())
                );
            }
        }

        if (!verifyInternalStructure(level, min, max)) {
            target.disconnect(true);
            return;
        }

        try {
            boolean updateGrid = false;
            SpawnerControllerCluster cluster = target.getCluster();
            if (cluster == null || !cluster.getBoundsMin().equals(min) || !cluster.getBoundsMax().equals(max)) {
                cluster = this.createCluster(level, min, max);
                setModificationInProgress(cluster);
                this.updateBlockEntities(cluster, level, min, max);
                updateGrid = true;
            } else {
                setModificationInProgress(cluster);
            }
            cluster.updateStatus(updateGrid);
        } finally {
            setModificationInProgress(null);
        }
    }

    @Override
    public SpawnerControllerCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new SpawnerControllerCluster(min, max);
    }

    @Override
    public void updateBlockEntities(SpawnerControllerCluster cluster, ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpawnerControllerBE part) {
                part.cluster = cluster;
                cluster.addBlockEntity(part);
            }
        }
        cluster.done();
        cluster.updateStatus(true);
    }


    @Override
    public boolean isValidBlockEntity(BlockEntity be) {
        return be instanceof SpawnerControllerBE;
    }

    public int countBlocks(Level level, BlockPos origin, Block targetBlock){
        return STRUCTURE_PATTERN.countBlocks(level, origin, targetBlock);
    }
}
