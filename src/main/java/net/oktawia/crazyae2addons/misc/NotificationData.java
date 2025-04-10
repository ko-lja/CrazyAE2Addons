package net.oktawia.crazyae2addons.misc;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.interfaces.ICustomNBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NotificationData implements ICustomNBTSerializable {
    public ArrayList<BlockEntityDescription> requesters = new ArrayList<>();

    public NotificationData(){
    }

    public void addRequester(AEBasePart requester){
        this.requesters.add(new BlockEntityDescription(requester.getLevel().dimensionTypeId().location(), requester.getBlockEntity().getBlockPos(), requester.getSide()));
    }
    public void addRequester(AEBaseBlockEntity requester){
        this.requesters.add(new BlockEntityDescription(requester.getLevel().dimensionTypeId().location(), requester.getBlockPos(), null));
    }

    public void removeRequester(AEBasePart requester){
        this.requesters.remove(new BlockEntityDescription(requester.getLevel().dimensionTypeId().location(), requester.getBlockEntity().getBlockPos(), requester.getSide()));
    }
    public void removeRequester(AEBaseBlockEntity requester){
        this.requesters.remove(new BlockEntityDescription(requester.getLevel().dimensionTypeId().location(), requester.getBlockPos(), null));
    }

    public String serialize(){
        return requesters.stream().map(this::serialize).collect(Collectors.joining("|"));
    }

    public String serialize(BlockEntityDescription BED) {
        String levelId = (BED.level == null) ? "null" : BED.level.toString();
        String posStr = BED.pos.getX() + "," + BED.pos.getY() + "," + BED.pos.getZ();
        String dirStr = (BED.dir == null) ? "null" : BED.dir.toString();
        return levelId + "!" + posStr + "!" + dirStr;
    }

    @Override
    public void deserialize(String data) {
        this.requesters = Arrays.stream(data.split(Pattern.quote("|"), -1))
                .map(this::deserialize_bed)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public BlockEntityDescription deserialize_bed(String data) {
        String[] parts = data.split(Pattern.quote("!"), -1);
        String levelId = parts[0];
        String[] posParts = parts[1].split(",", -1);
        int x = Integer.parseInt(posParts[0]);
        int y = Integer.parseInt(posParts[1]);
        int z = Integer.parseInt(posParts[2]);
        BlockPos pos = new BlockPos(x, y, z);
        Direction direction = "null".equals(parts[2]) ? null : Direction.valueOf(parts[2].toUpperCase());
        return new BlockEntityDescription(new ResourceLocation(levelId), pos, direction);
    }

    public static Object get(BlockEntityDescription bed, MinecraftServer server) {
        if (bed.level == null) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, bed.level);
        Level level = server.getLevel(key);
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(bed.pos);
        if (be == null) return null;
        if (bed.dir != null && be instanceof CableBusBlockEntity aeBe) {
            return aeBe.getPart(bed.dir);
        } else {
            return be;
        }
    }

    public record BlockEntityDescription(ResourceLocation level, BlockPos pos, Direction dir) {}
}