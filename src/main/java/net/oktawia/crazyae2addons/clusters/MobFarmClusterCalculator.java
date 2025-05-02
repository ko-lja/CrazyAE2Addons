package net.oktawia.crazyae2addons.clusters;

import appeng.me.cluster.MBCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.entities.MobFarmBE;

public class MobFarmClusterCalculator extends MBCalculator<MobFarmBE, MobFarmCluster> {

    public static final ClusterPattern STRUCTURE_PATTERN =
            new ClusterPattern(new ResourceLocation("crazyae2addons", "mob_farm"));

    public MobFarmClusterCalculator(MobFarmBE owner) {
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
    public MobFarmCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new MobFarmCluster(min, max);
    }

    @Override
    public void updateBlockEntities(MobFarmCluster cluster, ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MobFarmBE part) {
                cluster.addBlockEntity(part);
            }
        }
        cluster.done();
        cluster.updateStatus(true);
    }


    @Override
    public boolean isValidBlockEntity(BlockEntity be) {
        return be instanceof MobFarmBE;
    }

    public int countBlocks(Level level, BlockPos origin, Block targetBlock){
        return STRUCTURE_PATTERN.countBlocks(level, origin, targetBlock);
    }
}
