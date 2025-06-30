package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.oktawia.crazyae2addons.entities.EnergyStorageFrameBE;
import org.jetbrains.annotations.Nullable;

public class EnergyStorageFrame extends AEBaseEntityBlock<EnergyStorageFrameBE> {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public EnergyStorageFrame() {
        super(Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL));
        this.registerDefaultState(this.defaultBlockState().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyStorageFrameBE(pos, state);
    }
}
