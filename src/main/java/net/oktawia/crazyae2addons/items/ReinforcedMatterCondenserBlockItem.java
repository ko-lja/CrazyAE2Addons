package net.oktawia.crazyae2addons.items;

import appeng.block.AEBaseBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.entities.ReinforcedMatterCondenserBE;
import org.jetbrains.annotations.NotNull;

public class ReinforcedMatterCondenserBlockItem extends AEBaseBlockItem {
    public ReinforcedMatterCondenserBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
