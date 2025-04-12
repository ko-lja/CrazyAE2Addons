package net.oktawia.crazyae2addons.parts;

import appeng.api.config.*;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.p2p.P2PModels;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.EnergyExporterMenu;
import net.oktawia.crazyae2addons.network.DisplayValuePacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;


public class EnergyExporterPart extends UpgradeablePart implements
        IUpgradeableObject, IEnergyStorage, IGridTickable, MenuProvider, InternalInventoryHost {

    private final EnergyStorage energyStorage = new EnergyStorage(16777216, 0, 16777216);
    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private static final P2PModels MODELS = new P2PModels(
            new ResourceLocation(CrazyAddons.MODID, "part/energy_exporter"));
    public boolean greg;
    private EnergyExporterMenu menu;
    public int maxAmps;
    public int voltage;
    public String transfered;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public EnergyExporterPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1)
                .addService(IGridTickable.class,this);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        var inv = this.inv;
        if (inv != InternalInventory.empty()) {
            var opt = extra.getCompound("inv");
            for (int x = 0; x < inv.size(); x++) {
                var item = opt.getCompound("item" + x);
                inv.setItemDirect(x, ItemStack.of(item));
            }
        }
    }


    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        var inv = this.inv;
        if (inv != InternalInventory.empty()) {
            final CompoundTag opt = new CompoundTag();
            for (int x = 0; x < inv.size(); x++) {
                final CompoundTag item = new CompoundTag();
                final ItemStack is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.save(item);
                }
                opt.put("item" + x, item);
            }
            extra.put("inv", opt);
        }
    }

    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(Menus.ENERGY_EXPORTER_MENU, p, MenuLocators.forPart(this));
        }
        return true;
    }

        @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EnergyExporterMenu(containerId, playerInventory, this);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public void upgradesChanged() {
        switch (this.getUpgrades().getInstalledUpgrades(AEItems.SPEED_CARD)){
            case 1 -> maxAmps = 2;
            case 2 -> maxAmps = 4;
            case 3 -> maxAmps = 8;
            case 4 -> maxAmps = 16;
            default -> maxAmps = 1;
        }
        if (this.getMenu() != null){
            this.getMenu().maxAmps = this.maxAmps;
        }
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    protected int getUpgradeSlots() {
        return 4;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return maxExtract;
    }

    @Override
    public int getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> energyStorage).cast();
        return super.getCapability(cap);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        BlockEntity neighbor = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        transfered = "0";
        if (neighbor != null){
            if (this.greg) {
                neighbor.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, getSide().getOpposite()).ifPresent(storage -> {
                    double powerRequired = Math.min((long) voltage * maxAmps * FeCompat.ratio(false), storage.getEnergyCanBeInserted());
                    double availablePower = getGridNode().getGrid().getEnergyService().getStoredPower() * 2;
                    double maxPower = getGridNode().getGrid().getEnergyService().getMaxStoredPower() * 2;
                    if (((availablePower - powerRequired) * 100 / maxPower) > 33){
                        double ext = getGridNode().getGrid().getEnergyService().extractAEPower(powerRequired / 2, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        storage.acceptEnergyFromNetwork(getSide().getOpposite(), voltage, maxAmps);
                        transfered = Utils.shortenNumber(ext);
                    }
                });
            } else {
                neighbor.getCapability(ForgeCapabilities.ENERGY, getSide().getOpposite()).ifPresent(storage -> {
                    double powerRequired = Math.min(Math.pow(64, getInstalledUpgrades(AEItems.SPEED_CARD)), storage.getMaxEnergyStored() - storage.getEnergyStored());
                    double availablePower = getGridNode().getGrid().getEnergyService().getStoredPower() * 2;
                    double maxPower = getGridNode().getGrid().getEnergyService().getMaxStoredPower() * 2;
                    if (((availablePower - powerRequired) * 100 / maxPower) > 33){
                        double ext = getGridNode().getGrid().getEnergyService().extractAEPower(powerRequired / 2, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        storage.receiveEnergy((int) powerRequired, false);
                        transfered = Utils.shortenNumber(ext);
                    }
                });
            }
        }
        if (this.getMenu() != null){
            this.getMenu().transfered = transfered;
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.greg = !this.inv.getStackInSlot(0).isEmpty();
        if (this.getMenu() != null){
            this.getMenu().greg = this.greg;
        }
        if (!greg) return;
        Item tier = this.inv.getStackInSlot(0).getItem();
        if (tier == GTItems.BATTERY_LV_LITHIUM.asItem()){
            voltage = (int) Math.pow(2, 5);
        } else if (tier == GTItems.BATTERY_MV_LITHIUM.asItem()){
            voltage = (int) Math.pow(2, 7);
        } else if (tier == GTItems.BATTERY_HV_LITHIUM.asItem()){
            voltage = (int) Math.pow(2, 9);
        } else if (tier == GTItems.BATTERY_EV_VANADIUM.asItem()){
            voltage = (int) Math.pow(2, 11);
        } else if (tier == GTItems.BATTERY_IV_VANADIUM.asItem()){
            voltage = (int) Math.pow(2, 13);
        } else if (tier == GTItems.BATTERY_LuV_VANADIUM.asItem()){
            voltage = (int) Math.pow(2, 15);
        } else if (tier == GTItems.BATTERY_ZPM_NAQUADRIA.asItem()){
            voltage = (int) Math.pow(2, 17);
        } else if (tier == GTItems.BATTERY_UV_NAQUADRIA.asItem()){
            voltage = (int) Math.pow(2, 19);
        } else {
            voltage = 8;
        }
        if (this.getMenu() != null){
            this.getMenu().voltage = this.voltage;
        }
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return getUpgrades();
        }
        return super.getSubInventory(id);
    }

    public void setMenu(EnergyExporterMenu menu) {
        this.menu = menu;
    }

    public EnergyExporterMenu getMenu(){
        return this.menu;
    }
}