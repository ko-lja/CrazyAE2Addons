package net.oktawia.crazyae2addons.network;

import appeng.api.parts.IPartHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.parts.DisplayPart;

import java.util.HashMap;
import java.util.function.Supplier;

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

    public static void handle(DisplayValuePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity te = level.getBlockEntity(packet.pos);
                if (te instanceof IPartHost host) {
                    DisplayPart displayPart = (DisplayPart) host.getPart(packet.direction);
                    if (displayPart != null) {
                        displayPart.textValue = packet.textValue;
                        displayPart.spin = packet.spin;
                        HashMap<String, Integer> variablesMap = new HashMap<>();
                        for (String s : packet.variables.split("\\|")) {
                            String[] arr = s.split(":", 2);
                            variablesMap.put(arr[0], Integer.parseInt(arr[1]));
                        }
                        displayPart.variables = variablesMap;
                    }
                }
            };
        });
        ctx.setPacketHandled(true);
    }
}
