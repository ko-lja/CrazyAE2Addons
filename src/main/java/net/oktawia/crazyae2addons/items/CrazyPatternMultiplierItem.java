package net.oktawia.crazyae2addons.items;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartHelper;
import appeng.api.parts.SelectedPart;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
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
import net.minecraft.world.phys.Vec3;
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
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.isSecondaryUseActive() || ctx.getLevel().isClientSide)
            return InteractionResult.PASS;

        CompoundTag tag = ctx.getItemInHand().getTag();
        if (tag == null)
            return InteractionResult.FAIL;
        double mult = tag.getDouble("mult");
        IActionSource src = IActionSource.ofPlayer(ctx.getPlayer());

        IPart part = getClickedPart(ctx);
        if (part instanceof InterfaceLogicHost iih && handleInterface(iih, mult, src)
                || part instanceof PatternProviderLogicHost pph && handlePatternProvider(pph, mult)) {
            return InteractionResult.SUCCESS;
        }

        var be = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (be instanceof InterfaceLogicHost iih2 && handleInterface(iih2, mult, src)
                || be instanceof PatternProviderLogicHost pph2 && handlePatternProvider(pph2, mult)
                || be instanceof Container ctn && handleContainer(ctn, mult, be.getLevel())) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private static IPart getClickedPart(UseOnContext ctx) {
        Vec3 hit = ctx.getClickLocation()
                .subtract(ctx.getClickedPos().getX(), ctx.getClickedPos().getY(), ctx.getClickedPos().getZ());
        IPartHost host = PartHelper.getPartHost(ctx.getLevel(), ctx.getClickedPos());
        SelectedPart sel = host == null ? null : host.selectPartLocal(hit);
        return sel != null ? sel.part : null;
    }

    private static boolean handleInterface(InterfaceLogicHost host, double mult, IActionSource src) {
        var storage = host.getStorage();
        for (var stack : storage.getAvailableStacks()) {
            if (stack.getKey() instanceof AEItemKey key
                    && AEItems.PROCESSING_PATTERN.isSameAs(key.toStack())) {
                ItemStack old = key.toStack();
                ItemStack nw  = CrazyPatternMultiplierMenu.modify(old, mult, host.getBlockEntity().getLevel());
                long amt = stack.getLongValue();
                storage.extract(stack.getKey(), amt, Actionable.MODULATE, src);
                storage.insert(AEItemKey.of(nw), nw.getCount(), Actionable.MODULATE, src);
                return true;
            }
        }
        return false;
    }

    private static boolean handlePatternProvider(PatternProviderLogicHost host, double mult) {
        var inv = host.getTerminalPatternInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack is = inv.getStackInSlot(i);
            if (AEItems.PROCESSING_PATTERN.isSameAs(is)) {
                inv.setItemDirect(i, CrazyPatternMultiplierMenu.modify(is, mult, host.getBlockEntity().getLevel()));
                return true;
            }
        }
        return false;
    }

    private static boolean handleContainer(Container ctn, double mult, Level level) {
        for (int i = 0; i < ctn.getContainerSize(); i++) {
            ItemStack is = ctn.getItem(i);
            if (AEItems.PROCESSING_PATTERN.isSameAs(is)) {
                ctn.setItem(i, CrazyPatternMultiplierMenu.modify(is, mult, level));
                return true;
            }
        }
        return false;
    }
}
