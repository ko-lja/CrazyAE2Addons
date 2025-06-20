package net.oktawia.crazyae2addons.blocks;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class EnergyStorage64k extends AEBaseBlock {
    public EnergyStorage64k() {
        super(Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL));
    }
}
