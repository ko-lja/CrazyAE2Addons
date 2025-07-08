package net.oktawia.crazyae2addons.compat.CC;

import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPartItem;
import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL;

public class CCDataExtractorPart extends DataExtractorPart implements IGridTickable, MenuProvider {
    private static final boolean COMPUTERCRAFT_LOADED = ModList.get().isLoaded("computercraft");

    public CCDataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class, this);
    }

    @Override
    public String extractData() {
        if (target != null && available != null && selected >= 0 && selected < available.size()) {
            String key = available.get(selected);
            if (key.startsWith("cc:")) {
                return callCCMethod(key);
            }
        }
        return super.extractData();
    }

    private String callCCMethod(String key) {
        if (!COMPUTERCRAFT_LOADED || target == null || getSide() == null) return "";

        String raw = key.substring(3);
        String methodName = raw.endsWith("()") ? raw.substring(0, raw.length() - 2) : raw;

        return target.getCapability(CAPABILITY_PERIPHERAL, getSide().getOpposite())
                .filter(per -> per instanceof IDynamicPeripheral)
                .map(per -> {
                    IDynamicPeripheral dyn = (IDynamicPeripheral) per;
                    FakeComputerAccess computer = new FakeComputerAccess(identifier);
                    dyn.attach(computer);

                    String[] methods = dyn.getMethodNames();
                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].equals(methodName)) {
                            try {
                                FakeLuaContext context = new FakeLuaContext();
                                dyn.callMethod(computer, context, i, new ObjectArguments());
                                dyn.detach(computer);
                                Object[] vals = context.getLastResult();
                                if (vals != null && vals.length > 0) {
                                    return vals[0].toString();
                                }
                            } catch (LuaException ignored) {}
                            break;
                        }
                    }

                    dyn.detach(computer);
                    return "";
                })
                .orElse("");
    }

    @Override
    public void extractPossibleData() {
        super.extractPossibleData();

        if (!COMPUTERCRAFT_LOADED || target == null || getSide() == null) return;

        target.getCapability(CAPABILITY_PERIPHERAL, getSide().getOpposite()).ifPresent(per -> {
            if (per instanceof IDynamicPeripheral dyn) {
                List<String> combined = new ArrayList<>(available);
                FakeComputerAccess computer = new FakeComputerAccess(identifier);
                dyn.attach(computer);

                String[] methods = dyn.getMethodNames();
                for (int i = 0; i < methods.length; i++) {
                    String methodName = methods[i];
                    try {
                        FakeLuaContext context = new FakeLuaContext();
                        dyn.callMethod(computer, context, i, new ObjectArguments());
                        Object[] result = context.getLastResult();
                        if (result != null && result.length > 0) {
                            Object first = result[0];
                            if (first instanceof Number || first instanceof Boolean || first instanceof String) {
                                String entry = "cc:" + methodName + "()";
                                if (!combined.contains(entry)) {
                                    combined.add(entry);
                                }
                            }
                        }
                    } catch (LuaException ignored) {}
                }

                dyn.detach(computer);
                available = combined;
            }
        });

        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockEntity().getBlockPos())),
                    new DataValuesPacket(getBlockEntity().getBlockPos(), getSide(), available, selected, valueName)
            );
        }
    }


    private record FakeComputerAccess(String id) implements IComputerAccess {

        @Override
            public String getAttachmentName() {
                return id;
            }

            @Override
            public int getID() {
                return 0;
            }

            @Override
            public IPeripheral getAvailablePeripheral(String name) {
                return null;
            }

            @Override
            public Map<String, IPeripheral> getAvailablePeripherals() {
                return Collections.emptyMap();
            }

            @Override
            public dan200.computercraft.api.peripheral.WorkMonitor getMainThreadMonitor() {
                return new dan200.computercraft.api.peripheral.WorkMonitor() {
                    @Override
                    public boolean canWork() {
                        return true;
                    }

                    @Override
                    public boolean shouldWork() {
                        return true;
                    }

                    @Override
                    public void trackWork(long time, TimeUnit unit) {
                    }
                };
            }

            @Override
            public String mount(String desiredLocation, Mount mount) {
                return null;
            }

            @Override
            public String mount(String desiredLocation, Mount mount, String driveName) {
                return null;
            }

            @Override
            public String mountWritable(String desiredLocation, WritableMount mount) {
                return null;
            }

            @Override
            public String mountWritable(
                    String desiredLocation,
                    WritableMount mount,
                    String driveName
            ) {
                return null;
            }

            @Override
            public void unmount(@Nullable String s) {
            }

            @Override
            public void queueEvent(String event, Object... arguments) {
            }
        }

    private static class FakeLuaContext implements ILuaContext {
        private Object[] lastResult;

        @Override
        public long issueMainThreadTask(LuaTask task) {
            try {
                task.execute();
                Field resultField = task.getClass().getDeclaredField("result");
                resultField.setAccessible(true);
                lastResult = (Object[]) resultField.get(task);
            } catch (NoSuchFieldException | IllegalAccessException | LuaException e) {
                lastResult = null;
            }
            return 0L;
        }

        public Object[] getLastResult() {
            return lastResult;
        }
    }

}
