package net.oktawia.crazyae2addons.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CrazyBlockTagGenerator extends BlockTagsProvider {
    public CrazyBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CrazyAddons.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        for (var block : Blocks.getBlocks()){
            this.tag(BlockTags.NEEDS_IRON_TOOL)
                    .add(block.block());
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(block.block());
        }
    }
}
