package net.oktawia.crazyae2addons.compat.GregTech;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartItem;
import appeng.core.definitions.AEItems;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.parts.EnergyExporterPart;

public class GTEnergyExporterPart extends EnergyExporterPart {
    public GTEnergyExporterPart(IPartItem<?> partItem) {
        super(partItem);
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

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!initialized){
            upgradesChanged();
            onChangeInventory(this.inv, 0);
            initialized = true;
        }
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
                        transfered = Utils.shortenNumber(ext * 2);
                    }
                });
            } else {
                neighbor.getCapability(ForgeCapabilities.ENERGY, getSide().getOpposite()).ifPresent(storage -> {
                    double powerRequired = Math.min(Math.pow(64, getInstalledUpgrades(AEItems.SPEED_CARD)), storage.getMaxEnergyStored() - storage.getEnergyStored()) - 1;
                    double availablePower = getGridNode().getGrid().getEnergyService().getStoredPower() * 2;
                    double maxPower = getGridNode().getGrid().getEnergyService().getMaxStoredPower() * 2;
                    if (((availablePower - powerRequired) * 100 / maxPower) > 33){
                        double ext = getGridNode().getGrid().getEnergyService().extractAEPower(powerRequired / 2, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        storage.receiveEnergy((int) powerRequired, false);
                        transfered = Utils.shortenNumber(ext * 2);
                    }
                });
            }
        }
        if (this.getMenu() != null){
            this.getMenu().transfered = transfered;
        }
        return TickRateModulation.IDLE;
    }

}
