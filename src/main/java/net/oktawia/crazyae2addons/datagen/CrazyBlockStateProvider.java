package net.oktawia.crazyae2addons.datagen;

import appeng.core.definitions.BlockDefinition;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.Blocks;

public class CrazyBlockStateProvider extends BlockStateProvider {
    public CrazyBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CrazyAddons.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var block : Blocks.getBlocks()){
            if (block != Blocks.AMPERE_METER_BLOCK){
                simpleBlockWithItem(block);
            }
        }
    }

    private void simpleBlockWithItem(BlockDefinition<?> block) {
        simpleBlockWithItem(block.block(), cubeAll(block.block()));
    }
}
