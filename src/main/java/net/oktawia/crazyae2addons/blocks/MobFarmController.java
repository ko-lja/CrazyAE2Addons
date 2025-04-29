package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.oktawia.crazyae2addons.clusters.ClusterPattern;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.entities.MobFarmControllerBE;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MobFarmController extends AEBaseEntityBlock<MobFarmControllerBE> {

    private static ClusterPattern STRUCTURE_PATTERN;

    public static void init(){
        STRUCTURE_PATTERN = new ClusterPattern(
                "WWWWW/WWWWW/WWWWW/WWWWW/WWWWW|" +
                        "WWCWW/WLLLW/WLLLW/WLLLW/WWWWW|" +
                        "WWWWW/WDDDW/WDIDW/WDDDW/WWWWW|" +
                        "WWWWW/WDDDW/WDIDW/WDDDW/WWWWW|" +
                        "WWWWW/WWWWW/WWWWW/WWWWW/WWWWW",
                Map.of(
                        'C', Set.of(CrazyBlockRegistrar.MOB_FARM_CONTROLLER_BLOCK.get().asBlock()),
                        'W', Set.of(CrazyBlockRegistrar.MOB_FARM_WAll_BLOCK.get().defaultBlockState().getBlock()),
                        'L', Set.of(CrazyBlockRegistrar.MOB_FARM_COLLECTOR_BLOCK.get().defaultBlockState().getBlock()),
                        'D', Set.of(CrazyBlockRegistrar.MOB_FARM_DAMAGE_MODULE_BLOCK.get().defaultBlockState().getBlock(),
                                CrazyBlockRegistrar.MOB_FARM_WAll_BLOCK.get().defaultBlockState().getBlock()),
                        'I', Set.of(CrazyBlockRegistrar.MOB_FARM_INPUT_BLOCK.get().defaultBlockState().getBlock())
                )
        );
    }

    public MobFarmController() {
        super(AEBaseBlock.metalProps());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobFarmControllerBE(pos, state);
    }

    @Nullable
    public boolean tryFormCluster(Level level, BlockPos controllerPos) {
        if (level.isClientSide) {
            return false;
        }

        for (ClusterPattern.Rotation rotation : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(level, controllerPos, rotation)) {
                return true;
            }
        }
        return false;
    }

    public int countBlocksInCluster(Level level, BlockPos controllerPos, Block targetBlock) {
        if (level.isClientSide) {
            return 0;
        }

        for (ClusterPattern.Rotation rotation : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(level, controllerPos, rotation)) {
                BlockPos controllerOffset = STRUCTURE_PATTERN.findControllerOffset(rotation);
                BlockPos origin = controllerPos.subtract(controllerOffset);
                int count = 0;

                for (int y = 0; y < STRUCTURE_PATTERN.getLayers().size(); y++) {
                    List<String> layer = STRUCTURE_PATTERN.getLayers().get(y);
                    for (int z = 0; z < layer.size(); z++) {
                        String row = layer.get(z);
                        for (int x = 0; x < row.length(); x++) {
                            char symbol = row.charAt(x);
                            if (symbol == '.') continue;

                            int[] rotated = ClusterPattern.rotateXZ(x, z, rotation);
                            BlockPos checkPos = origin.offset(rotated[0], y, rotated[1]);

                            Block actualBlock = level.getBlockState(checkPos).getBlock();
                            if (actualBlock == targetBlock) {
                                count++;
                            }
                        }
                    }
                }
                return count;
            }
        }
        return 0;
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

        var be = level.getBlockEntity(pos);

        if (be instanceof MobFarmControllerBE MBPB) {
            if (MBPB.formed){
                if (!level.isClientSide()) {
                    MBPB.openMenu(player, MenuLocators.forBlockEntity(be));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}
