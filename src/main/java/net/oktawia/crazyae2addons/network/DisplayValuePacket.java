package net.oktawia.crazyae2addons.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

public class DisplayValuePacket {
    public final BlockPos pos;
    public final String textValue;
    public final Direction direction;
    public final byte spin;
    public final String variables;

    public DisplayValuePacket(BlockPos blockPos, String textValue, Direction partsDirection, byte spin, String variables) {
        this.pos = blockPos;
        this.textValue = textValue;
        this.direction = partsDirection;
        this.spin = spin;
        this.variables = variables;
    }

    public static void encode(DisplayValuePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.direction.toString());
        buf.writeUtf(packet.textValue);
        buf.writeByte(packet.spin);
        buf.writeUtf(packet.variables);
    }

    public static DisplayValuePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String dir = buf.readUtf();
        String textValue = buf.readUtf();
        byte spin = buf.readByte();
        String variables = buf.readUtf();
        Direction partsDirection = null;
        switch (dir){
            case "up" -> partsDirection = Direction.UP;
            case "down" -> partsDirection = Direction.DOWN;
            case "west" -> partsDirection = Direction.WEST;
            case "north" -> partsDirection = Direction.NORTH;
            case "south" -> partsDirection = Direction.SOUTH;
            case "east" -> partsDirection = Direction.EAST;
        }
        return new DisplayValuePacket(pos, textValue, partsDirection, spin, variables);
    }
}
