package net.oktawia.crazyae2addons.network;

import appeng.api.parts.IPartHost;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.Parts.DisplayPart;

import java.util.function.Supplier;

public class DisplayValuePacket {
    private final BlockPos pos;
    private final String textValue;
    private final Direction direction;
    private final byte spin;

    public DisplayValuePacket(BlockPos blockPos, String textValue, Direction partsDirection, byte spin) {
        this.pos = blockPos;
        this.textValue = textValue;
        this.direction = partsDirection;
        this.spin = spin;
    }

    public static void encode(DisplayValuePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.direction.toString());
        buf.writeUtf(packet.textValue);
        buf.writeByte(packet.spin);
    }

    public static DisplayValuePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String dir = buf.readUtf();
        String textValue = buf.readUtf();
        byte spin = buf.readByte();
        Direction partsDirection = null;
        switch (dir){
            case "up" -> partsDirection = Direction.UP;
            case "down" -> partsDirection = Direction.DOWN;
            case "west" -> partsDirection = Direction.WEST;
            case "north" -> partsDirection = Direction.NORTH;
            case "south" -> partsDirection = Direction.SOUTH;
            case "east" -> partsDirection = Direction.EAST;
        }
        return new DisplayValuePacket(pos, textValue, partsDirection, spin);
    }

    public static void handle(DisplayValuePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
             ClientLevel level = Minecraft.getInstance().level;
             if(level != null) {
                 BlockEntity te = level.getBlockEntity(packet.pos);
                 IPartHost host = (IPartHost) te;
                 DisplayPart displayPart = (DisplayPart) host.getPart(packet.direction);
                 if(displayPart != null) {
                     displayPart.textValue = packet.textValue;
                     displayPart.spin = packet.spin;
                 }
             }
        });
        ctx.setPacketHandled(true);
    }
}
