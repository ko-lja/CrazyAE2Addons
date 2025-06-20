package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

public class EnergyStorageFrame extends AEBaseBlock {

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
}
