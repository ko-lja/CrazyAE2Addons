package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class DenseEnergyStorage256k extends AEBaseBlock {
    public DenseEnergyStorage256k() {
        super(Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL));
    }
}
