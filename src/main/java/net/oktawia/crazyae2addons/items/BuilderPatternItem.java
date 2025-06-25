package net.oktawia.crazyae2addons.items;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.stacks.AEItemKey;
import appeng.items.AEBaseItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.logic.BuilderPatternHost;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BuilderPatternItem extends AEBaseItem implements IMenuItem {
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;

    public BuilderPatternItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level, @NotNull Player p, @NotNull InteractionHand hand) {
        if (!level.isClientSide() && p.isShiftKeyDown()) {
            MenuOpener.open(CrazyMenuRegistrar.BUILDER_PATTERN_MENU.get(), p, MenuLocators.forHand(p, hand));
        } else {
            ItemStack stack = p.getItemInHand(hand);
            if (!level.isClientSide() && pos1 != null && pos2 != null) {
                BlockPos min = new BlockPos(
                        Math.min(pos1.getX(), pos2.getX()),
                        Math.min(pos1.getY(), pos2.getY()),
                        Math.min(pos1.getZ(), pos2.getZ())
                );
                BlockPos max = new BlockPos(
                        Math.max(pos1.getX(), pos2.getX()),
                        Math.max(pos1.getY(), pos2.getY()),
                        Math.max(pos1.getZ(), pos2.getZ())
                );

                Map<String, Integer> blockMap = new LinkedHashMap<>();
                int blockIdCounter = 1;

                StringBuilder pattern = new StringBuilder();
                BlockPos cursor = min;

                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        for (int x = min.getX(); x <= max.getX(); x++) {
                            BlockPos current = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(current);
                            if (state.isAir()) continue;

                            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                            if (blockId == null) continue;
                            var itemKey = AEItemKey.of(state.getBlock().asItem());
                            if (itemKey.fuzzyEquals(AEItemKey.of(Blocks.AIR.asItem()), FuzzyMode.IGNORE_ALL)) continue;

                            StringBuilder fullId = new StringBuilder(blockId.toString());

                            if (!state.getValues().isEmpty()) {
                                fullId.append("[");
                                boolean first = true;
                                for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                                    if (!first) fullId.append(",");
                                    fullId.append(entry.getKey().getName()).append("=").append(entry.getValue());
                                    first = false;
                                }
                                fullId.append("]");
                            }

                            String key = fullId.toString();
                            if (!blockMap.containsKey(key)) {
                                blockMap.put(key, blockIdCounter++);
                            }

                            pattern.append(moveCursor(cursor, current));
                            pattern.append("P(").append(blockMap.get(key)).append(")");
                            cursor = current;
                        }
                    }
                }

                StringBuilder header = new StringBuilder();
                for (Map.Entry<String, Integer> entry : blockMap.entrySet()) {
                    header.append(entry.getValue()).append("(").append(entry.getKey()).append("),\n");
                }

                if (!header.isEmpty()) header.setLength(header.length() - 2);

                String finalCode = header + "\n||\n" + pattern;
                if (finalCode.length() <= 32767 || true){
                    stack.getOrCreateTag().putString("program", finalCode);
                    ProgramExpander.Result result = ProgramExpander.expand(finalCode);
                    if (result.success) {
                        stack.getOrCreateTag().putBoolean("code", true);
                        p.displayClientMessage(Component.literal("Saved pattern to NBT, length: " + finalCode.length()), true);
                    } else {
                        p.displayClientMessage(Component.literal("Could not save this structure"), true);
                    }
                } else {
                    p.displayClientMessage(Component.literal("Structure to big"), true);
                }

                pos1 = null;
                pos2 = null;
            }
            return InteractionResultHolder.success(stack);
        }
        return new InteractionResultHolder<>(
                InteractionResult.sidedSuccess(level.isClientSide()), p.getItemInHand(hand));
    }

    private String moveCursor(BlockPos from, BlockPos to) {
        StringBuilder moves = new StringBuilder();
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        while (dx > 0) { moves.append("E"); dx--; }
        while (dx < 0) { moves.append("W"); dx++; }
        while (dy > 0) { moves.append("U"); dy--; }
        while (dy < 0) { moves.append("D"); dy++; }
        while (dz > 0) { moves.append("S"); dz--; }
        while (dz < 0) { moves.append("N"); dz++; }

        return moves.toString();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player != null) {
            if (!context.isSecondaryUseActive()) {
                pos1 = pos;
                player.displayClientMessage(Component.literal("Corner 1 set!"), true);
            } else {
                pos2 = pos;
                player.displayClientMessage(Component.literal("Corner 2 set!"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new BuilderPatternHost(player, inventorySlot, stack);
    }
}