package net.oktawia.crazyae2addons.compat.CC;

import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPartItem;
import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.ModList;
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
        if (!COMPUTERCRAFT_LOADED || target == null || getSide() == null) {
            return "";
        }
        String methodName = key.substring(3, key.length() - 2);
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
                                dyn.callMethod(
                                        computer,
                                        context,
                                        i,
                                        new ObjectArguments()
                                );
                                Object[] vals = context.getLastResult();
                                if (vals != null && vals.length > 0) {
                                    Object v = vals[0];
                                    return v.toString();
                                }
                            } catch (LuaException ignored) {
                            }
                            break;
                        }
                    }

                    dyn.detach(computer);
                    return "";
                })
                .orElse("");
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
