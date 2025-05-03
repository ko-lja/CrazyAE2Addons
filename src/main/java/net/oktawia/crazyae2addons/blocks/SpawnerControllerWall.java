package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.clusters.SpawnerControllerCluster;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.SpawnerControllerClusterSyncPacket;
import net.oktawia.crazyae2addons.network.SpawnerControllerClusterSyncRequestPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnerControllerWall extends AEBaseEntityBlock<SpawnerControllerBE> {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public SpawnerControllerWall() {
        super(AEBaseEntityBlock.metalProps());
        this.registerDefaultState(this.defaultBlockState().setValue(FORMED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpawnerControllerBE(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            SpawnerControllerBE.tryFormCluster((ServerLevel) level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof SpawnerControllerBE be) {
                SpawnerControllerCluster cluster = be.getCluster();
                if (cluster != null && !cluster.isDestroyed()) {
                    List<ItemStack> toDrop = new ArrayList<>();
                    cluster.getInventory().forEach(is -> toDrop.add(is.copy()));
                    cluster.getUpgrades().forEach(up -> toDrop.add(up.copy()));
                    cluster.destroy();
                    ServerLevel srv = (ServerLevel) level;
                    for (ItemStack drop : toDrop) {
                        popResource(srv, pos, drop);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            SpawnerControllerBE be = getBlockEntity(level, pos);
            if (be != null && be.getCluster() != null) {
                SpawnerControllerCluster cluster = be.getCluster();
                List<ItemStack> toDrop = new ArrayList<>();
                cluster.getInventory().forEach(is -> toDrop.add(is.copy()));
                cluster.getUpgrades().forEach(up -> toDrop.add(up.copy()));
                ServerLevel srv = (ServerLevel) level;
                for (ItemStack drop : toDrop) {
                    popResource(srv, pos, drop);
                }
                be.getCluster().isDestroyed = false;
                be.getCluster().destroy();
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public InteractionResult onActivated(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            @Nullable ItemStack heldItem,
            BlockHitResult hit) {

        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        SpawnerControllerBE be = getBlockEntity(level, pos);
        if (be == null) {
            return InteractionResult.PASS;
        }

        SpawnerControllerCluster cluster = be.getCluster();
        if (cluster == null) {
            if (level.isClientSide()) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.SERVER.noArg(),
                        new SpawnerControllerClusterSyncRequestPacket(pos, level)
                );
            }
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            be.openMenu(player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}