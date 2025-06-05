package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;
import org.jetbrains.annotations.Nullable;

public class CraftingGuardBlock extends AEBaseEntityBlock<CraftingGuardBE> {
    public CraftingGuardBlock() {
        super(AEBaseBlock.metalProps());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraftingGuardBE(pos, state);
    }
}
