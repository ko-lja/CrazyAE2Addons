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
import net.minecraftforge.registries.ForgeRegistries;
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

public class DataExtractorPart extends AEBasePart implements IGridTickable, MenuProvider {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/import_bus_base");
    @PartModels public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_off"));
    @PartModels public static final IPartModel MODELS_ON  = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_on"));
    @PartModels public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_has_channel"));

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
        if (target == null) return;

        List<String> data = new ArrayList<>();

        if (target.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            data.add("percentFilled");
        }
        target.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(h -> {
            data.add("fluidPercentFilled");
            data.add("fluidAmount");
            data.add("fluidCapacity");
        });
        target.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
            data.add("storedEnergy");
            data.add("energyCapacity");
        });

        BlockPos pos = getBlockEntity().getBlockPos().relative(getSide());
        var level = getLevel();
        var state = level.getBlockState(pos);

        data.add("blockName");
        data.add("isAir");
        data.add("isSolid");
        data.add("redstonePower");
        data.add("blockLight");
        data.add("skyLight");

        for (var prop : state.getProperties()) {
            data.add("blockState:" + prop.getName());
        }

        try {
            state.getBlock().defaultDestroyTime();
            data.add("blockHardness");
        } catch (Exception ignored) {}

        try {
            state.getBlock().getExplosionResistance();
            data.add("blockExplosionResistance");
        } catch (Exception ignored) {}

        if (!data.equals(this.available)) {
            this.available = data;
        }

        this.resolveTarget = target;

        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockEntity().getBlockPos())),
                    new DataValuesPacket(getBlockEntity().getBlockPos(), getSide(), available, selected, valueName));
        }
    }



    public String extractData() {
        if (target == null) return "";
        if (available == null || available.isEmpty() || selected < 0 || selected >= available.size()) return "";

        String key = available.get(selected);
        BlockPos pos = getBlockEntity().getBlockPos().relative(getSide());
        var level = getLevel();
        var state = level.getBlockState(pos);

        return switch (key) {
            case "percentFilled" -> target.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(this::calcPercentFilled).orElse("");
            case "fluidPercentFilled" -> target.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .map(this::calcFluidPercent).orElse("");
            case "fluidAmount" -> target.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .map(h -> String.valueOf(h.getFluidInTank(0).getAmount())).orElse("");
            case "fluidCapacity" -> target.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .map(h -> String.valueOf(h.getTankCapacity(0))).orElse("");
            case "storedEnergy" -> target.getCapability(ForgeCapabilities.ENERGY)
                    .map(e -> String.valueOf(e.getEnergyStored())).orElse("");
            case "energyCapacity" -> target.getCapability(ForgeCapabilities.ENERGY)
                    .map(e -> String.valueOf(e.getMaxEnergyStored())).orElse("");
            case "redstonePower" -> String.valueOf(getLevel().getBestNeighborSignal(pos));
            case "blockLight" -> String.valueOf(getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos));
            case "skyLight" -> String.valueOf(getLevel().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos));
            case "blockName" -> {
                var blockreg = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                if (blockreg != null){
                    yield blockreg.getPath();
                }
                yield "error";
            }
            case "isAir" -> String.valueOf(state.isAir());
            case "isSolid" -> String.valueOf(state.isSolid());
            case "blockHardness" -> String.valueOf(state.getBlock().defaultDestroyTime());
            case "blockExplosionResistance" -> String.valueOf(state.getBlock().getExplosionResistance());
            default -> {
                if (key.startsWith("blockState:")) {
                    String propName = key.substring("blockState:".length());
                    for (var prop : state.getProperties()) {
                        if (prop.getName().equals(propName)) {
                            yield String.valueOf(state.getValue(prop));
                        }
                    }
                }
                yield "";
            }
        };
    }

    public String calcPercentFilled(IItemHandler h) {
        int slots = h.getSlots(), filled = 0;
        for (int i = 0; i < slots; i++) if (!h.getStackInSlot(i).isEmpty()) filled++;
        return slots == 0 ? "" : String.valueOf((filled * 100) / slots);
    }

    public String calcFluidPercent(IFluidHandler h) {
        if (h.getTanks() == 0) return "";
        int amt = h.getFluidInTank(0).getAmount();
        int cap = h.getTankCapacity(0);
        return cap == 0 ? "" : String.valueOf((amt * 100) / cap);
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

        if (!valueName.isEmpty()) {
            String name = valueName.replace("&", "");
            String val = extractData();
            ctrl.addVariable(identifier, this.getClass(), identifier, name, val);
        }

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
