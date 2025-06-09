package net.oktawia.crazyae2addons.parts;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class DataExtractorPart extends AEBasePart implements IGridTickable, MenuProvider, IUpgradeableObject {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/import_bus_base");
    @PartModels public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_off"));
    @PartModels public static final IPartModel MODELS_ON  = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_on"));
    @PartModels public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_has_channel"));

    private static final Map<Class<?>, List<String>> FIELD_CACHE   = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<String>> METHOD_CACHE  = new ConcurrentHashMap<>();


    public BlockEntity target;
    public List<String> available = List.of();
    public int selected = 0;
    public Object resolveTarget;
    public String identifier = randomHexId();

    public DataExtractorMenu menu;
    public String valueName = "";
    public int ticksWaited = 0;
    public int delay = 20;
    public DataValuesPacket packet = null;

    public DataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        if (extra.contains("available")) this.available = Arrays.asList(extra.getString("available").split("\\|"));
        if (extra.contains("identifier")) this.identifier = extra.getString("identifier");
        if (extra.contains("delay"))      this.delay      = extra.getInt("delay");
        if (extra.contains("selected"))   this.selected   = extra.getInt("selected");
        if (extra.contains("target")) {
            try { this.target = getLevel().getBlockEntity(BlockPos.of(extra.getLong("target"))); }
            catch (Exception ignored) {}
        }
        if (extra.contains("valuename"))  this.valueName  = extra.getString("valuename");
        if (!isClientSide()) {
            packet = new DataValuesPacket(getBlockEntity().getBlockPos(), getSide(), available, selected, valueName);
        }
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        extra.putString("available", available.isEmpty() ? "" : String.join("|", available));
        extra.putInt("selected", selected);
        extra.putInt("delay", delay);
        extra.putLong("target", getSide() == null ? 0 : getBlockEntity().getBlockPos().relative(getSide()).asLong());
        extra.putString("identifier", identifier);
        extra.putString("valuename", valueName);
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(Integer.toHexString(rand.nextInt(16)).toUpperCase());
        return sb.toString();
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        if (getSide() == null) return;
        target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        extractPossibleData();
        if (getMenu() != null) getMenu().available = String.join("\\|", available);
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (getSide() != null && pos.relative(getSide()).equals(neighbor)) {
            target = level.getBlockEntity(neighbor);
            extractPossibleData();
            if (getMenu() != null) {
                getMenu().available = String.join("|", available);
            }
        }
    }

    public void extractPossibleData() {
        if (target == null && getSide() != null) {
            target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        }
        if (target == null){
            return;
        }
        List<String> data = new ArrayList<>(extractNumericInfo(target));

        if (target.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            data.add("percentFilled");
        }
        if (target.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            data.add("fluidPercentFilled");
        }
        if (target.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
            data.add("storedEnergy");
        }

        if (!data.equals(this.available)) {
            this.available = data;
            if (selected >= available.size()) {
                selected = available.isEmpty() ? 0 : available.size() - 1;
            }
        }

        this.resolveTarget = target;

        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with( () -> getLevel().getChunkAt(getBlockEntity().getBlockPos())),
                    new DataValuesPacket(getBlockEntity().getBlockPos(), getSide(), available, selected, valueName));
        }
    }

    public Integer extractData() {
        if (target == null) {
            return 0;
        }
        if (available == null || available.isEmpty() || selected < 0 || selected >= available.size()) {
            return 0;
        }
        String key = available.get(selected);
        if ("percentFilled".equals(key)) {
            return target.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(this::calcPercentFilled).orElse(0);
        }
        if ("fluidPercentFilled".equals(key)) {
            return target.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .map(this::calcFluidPercent).orElse(0);
        }
        if ("storedEnergy".equals(key)) {
            return target.getCapability(ForgeCapabilities.ENERGY)
                    .map(IEnergyStorage::getEnergyStored).orElse(0);
        }
        try {
            return resolve(resolveTarget, key);
        } catch (Exception e) {
            return 0;
        }
    }

    public int calcPercentFilled(IItemHandler h) {
        int slots = h.getSlots(), filled = 0;
        for (int i = 0; i < slots; i++) if (!h.getStackInSlot(i).isEmpty()) filled++;
        return slots == 0 ? 0 : (filled * 100) / slots;
    }

    public int calcFluidPercent(IFluidHandler h) {
        if (h.getTanks() == 0) return 0;
        int amt = h.getFluidInTank(0).getAmount();
        int cap = h.getTankCapacity(0);
        return cap == 0 ? 0 : (amt * 100) / cap;
    }

    public Integer resolve(Object obj, String path) throws Exception {
        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            if (part.endsWith("()")) {
                String m = part.substring(0, part.length() - 2);
                Method method = current.getClass().getMethod(m);
                method.setAccessible(true);
                current = method.invoke(current);
            } else {
                Field f = getFieldRecursive(current.getClass(), part);
                current = f.get(current);
            }
        }
        if (current instanceof Number nmbr) {
            return nmbr.intValue();
        }
        if (current instanceof Boolean bln) {
            return bln ? 1 : 0;
        }
        return 0;
    }


    public static Field getFieldRecursive(Class<?> c, String name) throws NoSuchFieldException {
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    public List<String> extractNumericInfo(Object blockEntity) {
        return extractNumericInfoFromObject(blockEntity);
    }

    public List<String> extractNumericInfoFromObject(Object obj) {
        if (obj == null) return List.of();
        List<String> info = new ArrayList<>();
        try {
            info.addAll(extractFields(target));
            info.addAll(extractMethods(target));
            info = info.stream().distinct().toList();
        } catch (Throwable ignored) {}
        return info;
    }

    public List<String> extractFields(Object obj) {
        if (obj == null) return List.of();
        Class<?> cls = obj.getClass();

        List<String> cached = FIELD_CACHE.get(cls);
        if (cached != null) {
            return cached;
        }

        List<String> info = new ArrayList<>();
        String path = cls.getName().replace('.', '/') + ".class";
        try (InputStream in = cls.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                FIELD_CACHE.put(cls, List.of());
                return List.of();
            }
            ClassReader cr = new ClassReader(in);
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if (!isAllowedDescriptor(descriptor)) {
                        return super.visitField(access, name, descriptor, signature, value);
                    }
                    try {
                        Field f = cls.getDeclaredField(name);
                        f.setAccessible(true);
                        Object val = f.get(obj);
                        if (val instanceof Number || val instanceof Boolean) {
                            info.add(name);
                        }
                    } catch (Throwable ignored) {}
                    return super.visitField(access, name, descriptor, signature, value);
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException ignored) {}

        List<String> result = List.copyOf(info);
        FIELD_CACHE.put(cls, result);
        return result;
    }


    public List<String> extractMethods(Object obj) {
        if (obj == null) return List.of();
        Class<?> cls = obj.getClass();

        List<String> cached = METHOD_CACHE.get(cls);
        if (cached != null) {
            return cached;
        }

        List<String> info = new ArrayList<>();
        String path = cls.getName().replace('.', '/') + ".class";
        try (InputStream in = cls.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                METHOD_CACHE.put(cls, List.of());
                return List.of();
            }
            ClassReader cr = new ClassReader(in);
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (!descriptor.startsWith("()")) {
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                    String retDesc = descriptor.substring(2);
                    if (!isAllowedDescriptor(retDesc)) {
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                    try {
                        Method m = cls.getDeclaredMethod(name);
                        m.setAccessible(true);
                        Object val = m.invoke(obj);
                        if (val instanceof Number || val instanceof Boolean) {
                            info.add(name + "()");
                        }
                    } catch (Throwable ignored) {}
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException ignored) {}

        List<String> result = List.copyOf(info);
        METHOD_CACHE.put(cls, result);
        return result;
    }

    private boolean isAllowedDescriptor(String desc) {
        return desc.equals("I")   // int
                || desc.equals("J")   // long
                || desc.equals("S")   // short
                || desc.equals("B")   // byte
                || desc.equals("F")   // float
                || desc.equals("D")   // double
                || desc.equals("Z")   // boolean
                || desc.equals("Ljava/lang/Integer;")
                || desc.equals("Ljava/lang/Long;")
                || desc.equals("Ljava/lang/Short;")
                || desc.equals("Ljava/lang/Byte;")
                || desc.equals("Ljava/lang/Float;")
                || desc.equals("Ljava/lang/Double;")
                || desc.equals("Ljava/lang/Boolean;");
    }
    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6,6,11,10,10,13);
        bch.addBox(5,5,13,11,11,14);
        bch.addBox(4,4,14,12,12,16);
    }

    @Override
    public IPartModel getStaticModels() {
        if (isActive() && isPowered())   return MODELS_HAS_CHANNEL;
        if (isPowered())                 return MODELS_ON;
        return MODELS_OFF;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1,1,false,true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        try {
            extractPossibleData();
        } catch (Exception ignored) {}
        if (packet != null){
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with( () -> getLevel().getChunkAt(getBlockEntity().getBlockPos())), packet);
            packet = null;
        }
        ticksWaited++;
        if (ticksWaited < delay) return TickRateModulation.IDLE;
        ticksWaited = 0;
        if (target == null)
            target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        if (getGridNode()==null||getGridNode().getGrid()==null||
                getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty())
            return TickRateModulation.IDLE;
        MEDataControllerBE ctrl = getGridNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        if (!valueName.isEmpty())
            ctrl.addVariable(identifier, valueName.replace("&",""), extractData(), 0);
        return TickRateModulation.IDLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new DataExtractorMenu(id, inv, this);
    }
    @Override public Component getDisplayName() { return super.getDisplayName(); }

    public void setMenu(DataExtractorMenu m) { this.menu = m; }

    public DataExtractorMenu getMenu()      { return menu; }

    @Override
    public boolean onPartActivate(Player p, InteractionHand h, Vec3 pos) {
        if (!isClientSide())
            MenuOpener.open(CrazyMenuRegistrar.DATA_EXTRACTOR_MENU.get(), p, MenuLocators.forPart(this));
        return true;
    }
}
