package net.oktawia.crazyae2addons.misc;

import appeng.api.parts.IPart;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.function.Function;


public class BlockEntityDescription {
    public final Level level;
    public final BlockPos pos;
    @Nullable
    public final Direction dir;

    public BlockEntityDescription(AEBaseBlockEntity instance) {
        this.level = instance.getLevel();
        this.pos = instance.getBlockPos();
        this.dir = null;
    }

    public BlockEntityDescription(AEBasePart instance) {
        this.level = instance.getLevel();
        this.pos = instance.getBlockEntity().getBlockPos();
        this.dir = instance.getSide();
    }

    public BlockEntityDescription(Level level, BlockPos pos, @Nullable Direction dir) {
        this.level = level;
        this.pos = pos;
        this.dir = dir;
    }

    public @Nullable Object get() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        if (dir != null && be instanceof CableBusBlockEntity aeBe) {
            return aeBe.getPart(dir);
        } else {
            return be;
        }
    }

    public String serialize() {
        String levelId = level != null ? level.dimension().location().toString() : "null";
        String posStr = pos.getX() + "," + pos.getY() + "," + pos.getZ();
        String dirStr = dir != null ? dir.name() : "null";
        return levelId + "|" + posStr + "|" + dirStr;
    }

    public static BlockEntityDescription deserialize(String input, Function<String, Level> levelResolver) {
        String[] parts = input.split("\\|");

        String levelId = parts[0];
        String[] posParts = parts[1].split(",");

        Level level = "null".equals(levelId) ? null : levelResolver.apply(levelId);
        BlockPos pos = new BlockPos(
                Integer.parseInt(posParts[0]),
                Integer.parseInt(posParts[1]),
                Integer.parseInt(posParts[2])
        );

        Direction dir = "null".equals(parts[2]) ? null : Direction.valueOf(parts[2]);

        return new BlockEntityDescription(level, pos, dir);
    }
}