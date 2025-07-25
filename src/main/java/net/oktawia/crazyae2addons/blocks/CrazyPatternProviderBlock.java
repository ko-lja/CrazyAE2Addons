package net.oktawia.crazyae2addons.blocks;

import appeng.block.crafting.PatternProviderBlock;
import appeng.core.definitions.AEBlocks;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.entities.CrazyPatternProviderBE;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.SyncBlockClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrazyPatternProviderBlock extends PatternProviderBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrazyPatternProviderBE(pos, state);
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player,
                                         InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity myBe = level.getBlockEntity(pos);
        if (myBe instanceof CrazyPatternProviderBE crazyProvider) {
            var added = crazyProvider.getAdded();
            if(!level.isClientSide){
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new SyncBlockClientPacket(pos, added));
            }
        }

        if (heldItem != null && heldItem.getItem() == CrazyItemRegistrar.CRAZY_UPGRADE.get().asItem()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE crazyProvider) {
                crazyProvider.incrementAdded();
                var added = crazyProvider.getAdded();
                var newBe = crazyProvider.refreshLogic(added);
                newBe.setAdded(added);
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);

                ItemStack inHand = player.getItemInHand(hand);
                inHand.shrink(1);
                player.setItemInHand(hand, inHand.isEmpty() ? ItemStack.EMPTY : inHand);
                if(!level.isClientSide){
                    NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                            new SyncBlockClientPacket(pos, added));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (heldItem != null && InteractionUtil.canWrenchRotate(heldItem)) {
            setSide(level, pos, hit.getDirection());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        var be = this.getBlockEntity(level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                be.openMenu(player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof CrazyPatternProviderBE myBe) {
            ItemStack stack = new ItemStack(this);
            CompoundTag tag = new CompoundTag();
            tag.putInt("added", myBe.getAdded());
            stack.setTag(tag);
            return List.of(stack);
        }

        return super.getDrops(state, builder);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE myBe && stack.hasTag()) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("added")) {
                    myBe.loadTag(tag);
                    NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                            new SyncBlockClientPacket(pos, tag.getInt("added")));
                }
            }
        }
    }
}
