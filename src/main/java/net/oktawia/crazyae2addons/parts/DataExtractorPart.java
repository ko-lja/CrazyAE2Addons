package net.oktawia.crazyae2addons.parts;

import appeng.api.networking.GridFlags;
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
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.mojang.logging.LogUtils;
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
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DataExtractorPart extends AEBasePart implements IGridTickable, MenuProvider, IUpgradeableObject {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_has_channel"));

    public BlockEntity target;
    public List<String> available = List.of();
    public Integer selected = 0;
    public Object resolveTarget;
    public String identifier;

    public DataExtractorMenu menu;
    public String valueName = "";
    public Integer ticksWaited = 0;
    public Integer delay = 20;

    public DataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        if(extra.contains("available")){
            this.available = Arrays.stream(extra.getString("available").split("\\|")).toList();
        }
        if(extra.contains("identifier")){
            this.identifier = extra.getString("identifier");
        }
        if(extra.contains("delay")){
            this.delay = extra.getInt("delay");
        }
        if(extra.contains("selected")){
            this.selected = extra.getInt("selected");
        }
        if(extra.contains("target")){
            try {
                this.target = getLevel().getBlockEntity(BlockPos.of(extra.getLong("target")));
            } catch (Exception ignored) {}
        }
        if(extra.contains("valuename")){
            this.valueName = extra.getString("valuename");
        }
        if (!isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new DataValuesPacket(this.getBlockEntity().getBlockPos(), this.getSide(), this.available, this.selected, this.valueName));
        }
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        extra.putString("available", String.join("|", this.available));
        extra.putInt("selected", this.selected);
        extra.putInt("delay", this.delay);
        extra.putLong("target", this.getBlockEntity().getBlockPos().relative(getSide()).asLong());
        extra.putString("identifier", this.identifier);
        extra.putString("valuename", this.valueName);

    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int val = rand.nextInt(16); // 0-15
            sb.append(Integer.toHexString(val).toUpperCase());
        }
        return sb.toString();
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        extractPossibleData();
        if (this.getMenu() != null){
            this.getMenu().available = String.join("\\|", this.available);
        }
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(getSide()).equals(neighbor)) {
            this.valueName = "";
            this.selected = 0;
            this.target = level.getBlockEntity(neighbor);
            extractPossibleData();
            if (this.getMenu() != null){
                this.getMenu().available = String.join("|", this.available);
            }
        }
    }

    public void extractPossibleData(){
        if (this.target == null){
            this.target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        }
        if (this.target instanceof MetaMachineBlockEntity){
            var gtMachine = SimpleTieredMachine.getMachine(getLevel(), target.getBlockPos());
            if (gtMachine == null) return;
            var machineClass = gtMachine.getClass();
            boolean useLogic = false;
            Field field;
            RecipeLogic recLogic = null;

            try {
                field = machineClass.getSuperclass().getDeclaredField("recipeLogic");
                field.setAccessible(true);
                try {
                    recLogic = (RecipeLogic) field.get(gtMachine);
                    useLogic = true;
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            if (useLogic){
                this.available = extractNumericInfo(recLogic);
            } else {
                this.available = extractNumericInfo(gtMachine);
            }
            RecipeLogic finalRecLogic = recLogic;
            boolean finalUseLogic = useLogic;
            if (finalUseLogic){
                this.resolveTarget = finalRecLogic;
            } else {
                this.resolveTarget = gtMachine;
            }
        } else {
            this.resolveTarget = this.target;
            this.available = extractNumericInfo(this.target);
        }
        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new DataValuesPacket(this.getBlockEntity().getBlockPos(), this.getSide(), this.available, this.selected, this.valueName));
        }
    }

    public Integer extractData(){
        if (this.resolveTarget == null){
            extractPossibleData();
        }
        try {
            return resolve(this.resolveTarget, this.available.get(this.selected));
        } catch (Exception ignored) {}
        return 0;
    }

    public static Integer resolve(Object obj, String path) throws Exception {
        String[] parts = path.split("\\.");
        Object current = obj;

        for (String part : parts) {
            if (part.endsWith("()")) {
                String methodName = part.substring(0, part.length() - 2);
                Method method = current.getClass().getMethod(methodName);
                method.setAccessible(true);
                current = method.invoke(current);
            } else {
                Field field = getFieldRecursive(current.getClass(), part);
                current = field.get(current);
            }
        }
        return current != null ? (Integer) current : null;
    }

    private static Field getFieldRecursive(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
    }

    public List<String> extractNumericInfo(Object blockEntity) {
        return extractNumericInfoFromObject(blockEntity, "");
    }
    private List<String> extractNumericInfoFromObject(Object obj, String prefix) {
        if (obj == null) return new ArrayList<>();
        List<String> info = new ArrayList<>();
        info.addAll(extractFields(obj, prefix));
        info.addAll(extractMethods(obj, prefix));
        return info;
    }

    private List<String> extractFields(Object obj, String prefix) {
        List<String> info = new ArrayList<>();
        Class<?> currentClass = obj.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value instanceof Number) {
                        info.add(prefix + field.getName());
                    } else if (value != null && isInventoryType(value)) {
                        extractNumericInfoFromObject(value, prefix + "  ")
                                .forEach(extracted -> info.add(prefix + field.getName() + "." + extracted));
                    }
                } catch (IllegalAccessException e) {
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return info;
    }

    private List<String> extractMethods(Object obj, String prefix) {
        List<String> info = new ArrayList<>();
        Method[] methods = obj.getClass().getDeclaredMethods();
        String[] bannedMethods = {"saveAdditional", "loadTag", "writeToNBT", "readFromNBT"};
        for (Method method : methods) {
            if (shouldSkipMethod(method, bannedMethods)) continue;
            method.setAccessible(true);
            try {
                Object result = method.invoke(obj);
                if (result instanceof Number) {
                    info.add(prefix + method.getName() + "()");
                } else if (result != null && isInventoryType(result)) {
                    extractNumericInfoFromObject(result, prefix + "  ")
                            .forEach(extracted -> info.add(prefix + method.getName() + "()." + extracted));
                }
            } catch (Exception ignored) {}
        }
        return info;
    }

    private boolean shouldSkipMethod(Method method, String[] bannedMethods) {
        if (method.getParameterCount() > 0) return true;
        String name = method.getName();
        if (!(name.startsWith("get") || name.startsWith("is"))) return true;
        String lowerName = name.toLowerCase();
        for (String banned : bannedMethods) {
            if (lowerName.equals(banned.toLowerCase()) || lowerName.contains("onready"))
                return true;
        }
        return false;
    }


    private static boolean isInventoryType(Object obj) {
        try {
            Class<?> baseInventory = Class.forName("BaseInventory");
            return baseInventory.isAssignableFrom(obj.getClass());
        } catch (ClassNotFoundException e) {
            return obj.getClass().getSimpleName().toLowerCase().contains("inventory");
        }
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.ticksWaited ++;
        if (this.ticksWaited < this.delay){
            return TickRateModulation.IDLE;
        }
        this.ticksWaited = 0;
        if (this.identifier == null){
            this.identifier = randomHexId();
        }
        if (this.target == null){
            this.target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        }
        if (this.getGridNode() == null || this.getGridNode().getGrid() == null || this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()){
            return TickRateModulation.IDLE;
        }
        MEDataControllerBE controller = this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        if (!this.valueName.isEmpty()){
            controller.addVariable(this.identifier, this.valueName.replace("&", ""), extractData(), 0);
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DataExtractorMenu(containerId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void setMenu(DataExtractorMenu menu){
        this.menu = menu;
    }
    public DataExtractorMenu getMenu(){
        return this.menu;
    }

    @Override
    public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(Menus.DATA_EXTRACTOR_MENU, p, MenuLocators.forPart(this));
        }
        return true;
    }
}

