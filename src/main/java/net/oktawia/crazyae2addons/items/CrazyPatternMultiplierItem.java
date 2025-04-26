package net.oktawia.crazyae2addons.items;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.logic.CrazyPatternMultiplierHost;
import net.oktawia.crazyae2addons.menus.CrazyPatternMultiplierMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrazyPatternMultiplierItem extends AEBaseItem implements IMenuItem {
    public CrazyPatternMultiplierItem(Properties properties) {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level, @NotNull Player p, @NotNull InteractionHand hand) {
        if (!level.isClientSide() && !p.isSecondaryUseActive()) {
            MenuOpener.open(CrazyMenuRegistrar.CRAZY_PATTERN_MULTIPLIER_MENU.get(), p, MenuLocators.forHand(p, hand));
        }
        return new InteractionResultHolder<>(
                InteractionResult.sidedSuccess(level.isClientSide()), p.getItemInHand(hand));
    }

    @Override
    public @Nullable ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new CrazyPatternMultiplierHost(player, inventorySlot, stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx){
        if (!ctx.isSecondaryUseActive() || ctx.getLevel().isClientSide) return InteractionResult.PASS;
        BlockEntity block = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (block != null){
            CompoundTag tag = ctx.getItemInHand().getTag();
            if (tag == null) return InteractionResult.FAIL;
            double multiplier = tag.getDouble("mult");
            if (block instanceof Container container){
                for (int i = 0; i < container.getContainerSize(); i++){
                    ItemStack is = container.getItem(i);
                    if (AEItems.PROCESSING_PATTERN.isSameAs(is)){
                        ItemStack newIs = CrazyPatternMultiplierMenu.modify(is, multiplier, ctx.getLevel());
                        container.setItem(i, newIs);
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (block instanceof AEBaseInvBlockEntity container) {
                for (int i = 0; i < container.getInternalInventory().size(); i++){
                    ItemStack is = container.getInternalInventory().getStackInSlot(i);
                    if (AEItems.PROCESSING_PATTERN.isSameAs(is)){
                        ItemStack newIs = CrazyPatternMultiplierMenu.modify(is, multiplier, ctx.getLevel());
                        container.getInternalInventory().setItemDirect(i, newIs);
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (block instanceof PatternProviderBlockEntity provider){
                for (int i = 0; i < provider.getTerminalPatternInventory().size(); i++){
                    ItemStack is = provider.getTerminalPatternInventory().getStackInSlot(i);
                    if (AEItems.PROCESSING_PATTERN.isSameAs(is)){
                        ItemStack newIs = CrazyPatternMultiplierMenu.modify(is, multiplier, ctx.getLevel());
                        provider.getTerminalPatternInventory().setItemDirect(i, newIs);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
