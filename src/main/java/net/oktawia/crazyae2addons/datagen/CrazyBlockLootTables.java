package net.oktawia.crazyae2addons.datagen;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.oktawia.crazyae2addons.defs.Blocks;

import java.util.Set;

public class CrazyBlockLootTables extends BlockLootSubProvider {
    public CrazyBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (var block : Blocks.getBlocks()){
            this.dropSelf(block.block());
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks(){
        return Blocks.getBlocks().stream().map(b -> (Block) b.block())::iterator;
    }

}
