package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.menus.CrazyPatternProviderMenu;
import net.oktawia.crazyae2addons.screens.CrazyPatternProviderScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdatePatternsPacket {
    private final List<ItemStack> patterns;

    public UpdatePatternsPacket(List<ItemStack> patterns) {
        this.patterns = patterns;
    }

    public static void encode(UpdatePatternsPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.patterns.size());
        for (ItemStack stack : packet.patterns){
            buf.writeItemStack(stack, false);
        }
    }

    public static UpdatePatternsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        var patterns = new ArrayList<ItemStack>(size);
        for (int i = 0; i < size; i++) {
            patterns.add(buf.readItem());
        }
        return new UpdatePatternsPacket(patterns);
    }

    public static void handle(UpdatePatternsPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof CrazyPatternProviderScreen<?> screen) {
                screen.updatePatternsFromServer(packet.patterns);
            }
        });
        ctx.setPacketHandled(true);
    }
}
