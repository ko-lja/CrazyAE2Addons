package net.oktawia.crazyae2addons.network;

import appeng.api.parts.IPartHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;
import net.oktawia.crazyae2addons.parts.DisplayPart;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {

    public static void handleDisplayValuePacket(DisplayValuePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
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
                }
            });
        });
        ctx.setPacketHandled(true);
    }

    public static void handleDataValuesPacket(DataValuesPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
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
        });
        ctx.setPacketHandled(true);
    }
}
