package net.oktawia.crazyae2addons.items;

import appeng.block.AEBaseBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.oktawia.crazyae2addons.defs.Blocks;

public class CraftingCancelerBlockItem extends AEBaseBlockItem {
    public CraftingCancelerBlockItem(Properties properties) {
        super(Blocks.CRAFTING_CANCELER_BLOCK.block(), properties);
    }

    public CraftingCancelerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}