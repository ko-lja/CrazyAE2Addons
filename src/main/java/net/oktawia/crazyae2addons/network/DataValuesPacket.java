package net.oktawia.crazyae2addons.network;

import appeng.api.parts.IPartHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DataValuesPacket {
    public final BlockPos pos;
    public final String data;
    public final Direction direction;
    public final Integer selected;
    public final String valueName;

    public DataValuesPacket(BlockPos pos, Direction dir, List<String> data, Integer selected, String valueName) {
        this.pos = pos;
        this.direction = dir;
        this.data = String.join("|", data);
        this.selected = selected;
        this.valueName = valueName;
    }

    public static DataValuesPacket decode(FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var dir = buf.readUtf();
        var data = Arrays.stream(buf.readUtf().split("\\|")).toList(); // string
        var selected = buf.readInt();
        var valueName = buf.readUtf();
        Direction partsDirection = null;
        switch (dir){
            case "up" -> partsDirection = Direction.UP;
            case "down" -> partsDirection = Direction.DOWN;
            case "west" -> partsDirection = Direction.WEST;
            case "north" -> partsDirection = Direction.NORTH;
            case "south" -> partsDirection = Direction.SOUTH;
            case "east" -> partsDirection = Direction.EAST;
        }
        var direction = partsDirection;

        return new DataValuesPacket(pos, direction, data, selected, valueName);
    }

    public static void encode(DataValuesPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.direction.toString());
        buf.writeUtf(packet.data);
        buf.writeInt(packet.selected);
        buf.writeUtf(packet.valueName);
    }

    public static void handle(DataValuesPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity te = level.getBlockEntity(packet.pos);
                if (te instanceof IPartHost host) {
                    DataExtractorPart extractor = (DataExtractorPart) host.getPart(packet.direction);
                    if (extractor != null) {
                        extractor.available = Arrays.stream(packet.data.split("\\|")).toList();
                        extractor.selected = packet.selected;
                        extractor.valueName = packet.valueName;
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}