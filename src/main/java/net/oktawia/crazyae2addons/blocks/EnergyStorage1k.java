package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class EnergyStorage1k extends AEBaseBlock {
    public EnergyStorage1k() {
        super(Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL));
    }
}
