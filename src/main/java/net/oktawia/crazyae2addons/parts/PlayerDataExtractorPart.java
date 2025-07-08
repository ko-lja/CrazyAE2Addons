package net.oktawia.crazyae2addons.parts;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import net.oktawia.crazyae2addons.menus.PlayerDataExtractorMenu;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerDataExtractorPart extends AEBasePart implements IGridTickable, MenuProvider {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/import_bus_base");
    @PartModels public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_off"));
    @PartModels public static final IPartModel MODELS_ON  = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_on"));
    @PartModels public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/import_bus_has_channel"));

    public UUID target;
    public List<String> available = List.of();
    public int selected = 0;
    public String identifier = randomHexId();

    public PlayerDataExtractorMenu menu;
    public String valueName = "";
    public int ticksWaited = 0;
    public int delay = 20;
    public DataValuesPacket packet = null;
    public boolean playerMode = false;

    public PlayerDataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setIdlePowerUsage(16)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        if (extra.contains("available")) this.available = Arrays.asList(extra.getString("available").split("\\|"));
        if (extra.contains("identifier")) this.identifier = extra.getString("identifier");
        if (extra.contains("delay"))      this.delay      = extra.getInt("delay");
        if (extra.contains("selected"))   this.selected   = extra.getInt("selected");
        if (extra.contains("target"))     this.target     = extra.getUUID("target");
        if (extra.contains("valuename"))  this.valueName  = extra.getString("valuename");
        if (extra.contains("playerMode")) this.playerMode = extra.getBoolean("playerMode");
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
        extra.putUUID("target", this.target);
        extra.putString("identifier", identifier);
        extra.putString("valuename", valueName);
        extra.putBoolean("playerMode", playerMode);
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
        target = player.getUUID();
        extractPossibleData();
        if (getMenu() != null) getMenu().available = String.join("\\|", available);
    }

    public void extractPossibleData() {
        List<String> data = new ArrayList<>();
        data.add("playerName");
        data.add("playerHealth");
        data.add("playerMaxHealth");
        data.add("playerDistance");
        data.add("playerIsSneaking");
        data.add("playerIsSprinting");
        data.add("playerYaw");
        data.add("playerPitch");

        this.available = data;
        if (selected >= available.size()) {
            selected = available.isEmpty() ? 0 : available.size() - 1;
        }

        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockEntity().getBlockPos())),
                    new DataValuesPacket(getBlockEntity().getBlockPos(), getSide(), available, selected, valueName));
        }
    }

    public String extractData() {
        if (available == null || available.isEmpty() || selected < 0 || selected >= available.size()) return "";

        String key = available.get(selected);
        var level = getLevel();

        Player player = playerMode ? level.getPlayerByUUID(target) :
                level.getNearestPlayer(getBlockEntity().getBlockPos().getX(), getBlockEntity().getBlockPos().getY(), getBlockEntity().getBlockPos().getZ(), 64, true);

        if (player == null) return "";

        return switch (key) {
            case "playerName" -> player.getName().getString();
            case "playerHealth" -> String.valueOf(player.getHealth());
            case "playerMaxHealth" -> String.valueOf(player.getMaxHealth());
            case "playerDistance" -> {
                double dist = Math.sqrt(player.distanceToSqr(getBlockEntity().getBlockPos().getCenter()));
                yield String.format("%.2f", dist);
            }
            case "playerIsSneaking" -> String.valueOf(player.isCrouching());
            case "playerIsSprinting" -> String.valueOf(player.isSprinting());
            case "playerYaw" -> String.format("%.2f", player.getYRot());
            case "playerPitch" -> String.format("%.2f", player.getXRot());
            default -> "";
        };
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
        return new PlayerDataExtractorMenu(id, inv, this);
    }
    @Override public Component getDisplayName() { return super.getDisplayName(); }

    public void setMenu(PlayerDataExtractorMenu m) { this.menu = m; }

    public PlayerDataExtractorMenu getMenu()      { return menu; }

    @Override
    public boolean onPartActivate(Player p, InteractionHand h, Vec3 pos) {
        if (!isClientSide())
            MenuOpener.open(CrazyMenuRegistrar.PLAYER_DATA_EXTRACTOR_MENU.get(), p, MenuLocators.forPart(this));
        return true;
    }
}
