package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.AmpereMeterMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AmpereMeterBE extends AEBaseBlockEntity implements MenuProvider {

    public AmpereMeterMenu menu;
    public boolean direction = false;
    public String transfer = "-";
    public String unit = "-";
    public Integer numTransfer = 0;
    public HashMap<Integer, Integer> average = new HashMap<>();

    public AmpereMeterBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public void setMenu(AmpereMeterMenu menu){
        this.menu = menu;
    }

    public AmpereMeterMenu getMenu(){
        return this.menu;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player pPlayer) {
        return new AmpereMeterMenu(i, inventory, this);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if(data.contains("dir")){
            this.direction = data.getBoolean("dir");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data){
        super.saveAdditional(data);
        data.putBoolean("dir", this.direction);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Ampere Meter");
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.AMPERE_METER_MENU, player, locator);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction dir) {
        Direction inputSide = this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
        Direction outputSide = !this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
        if (cap == ForgeCapabilities.ENERGY && dir == inputSide) {
            return LazyOptional.of(() -> feLogicInput).cast();
        } else if (cap == ForgeCapabilities.ENERGY && dir == outputSide) {
            return LazyOptional.of(() -> feLogicOutput).cast();
        } else if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER){
            return LazyOptional.of(() -> euLogic).cast();
        }
        return super.getCapability(cap, dir);
    }

    public IEnergyStorage feLogicInput = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            if (AmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !AmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = AmpereMeterBE.this.getLevel().getBlockEntity(AmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicInteger transferred = new AtomicInteger();
            output.getCapability(ForgeCapabilities.ENERGY, outputSide.getOpposite()).ifPresent(out -> {
                transferred.set(out.receiveEnergy(maxReceive, simulate));
            });
            if (!Objects.equals(AmpereMeterBE.this.unit, "FE/t")){
                AmpereMeterBE.this.average.clear();
                AmpereMeterBE.this.unit = "FE/t";
            }
            if (AmpereMeterBE.this.average.size() >= 5){
                int trans = AmpereMeterBE.this.average.values().stream().reduce(0, Integer::sum)/AmpereMeterBE.this.average.size();
                AmpereMeterBE.this.transfer = Utils.shortenNumber(trans);
                AmpereMeterBE.this.numTransfer = trans;
                AmpereMeterBE.this.average.clear();
                if (AmpereMeterBE.this.getMenu() != null){
                    AmpereMeterBE.this.getMenu().unit = AmpereMeterBE.this.unit;
                    AmpereMeterBE.this.getMenu().transfer = AmpereMeterBE.this.transfer;
                }
            }
            AmpereMeterBE.this.average.put(AmpereMeterBE.this.average.size(), transferred.get());
            return transferred.get();
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    };

    public IEnergyStorage feLogicOutput = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            if (AmpereMeterBE.this.getLevel() == null) return 0;
            Direction inputSide = AmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity input = AmpereMeterBE.this.getLevel().getBlockEntity(AmpereMeterBE.this.getBlockPos().relative(inputSide));
            if (input == null) return 0;
            AtomicInteger transferred = new AtomicInteger();
            input.getCapability(ForgeCapabilities.ENERGY, inputSide.getOpposite()).ifPresent(out -> {
                transferred.set(out.receiveEnergy(maxExtract, simulate));
            });
            if (!Objects.equals(AmpereMeterBE.this.unit, "FE/t")){
                AmpereMeterBE.this.average.clear();
                AmpereMeterBE.this.unit = "FE/t";
            }
            if (AmpereMeterBE.this.average.size() >= 5){
                int trans = AmpereMeterBE.this.average.values().stream().reduce(0, Integer::sum)/AmpereMeterBE.this.average.size();
                AmpereMeterBE.this.transfer = Utils.shortenNumber(trans);
                AmpereMeterBE.this.numTransfer = trans;
                AmpereMeterBE.this.average.clear();
                if (AmpereMeterBE.this.getMenu() != null){
                    AmpereMeterBE.this.getMenu().unit = AmpereMeterBE.this.unit;
                    AmpereMeterBE.this.getMenu().transfer = AmpereMeterBE.this.transfer;
                }
            }
            AmpereMeterBE.this.average.put(AmpereMeterBE.this.average.size(), transferred.get());
            return transferred.get();
        }
        @Override public int getEnergyStored() { return Integer.MAX_VALUE; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    };

    public IEnergyContainer euLogic = new IEnergyContainer() {
        @Override public long acceptEnergyFromNetwork(Direction side, long volt, long amp) {
            if (AmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !AmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = AmpereMeterBE.this.getLevel().getBlockEntity(AmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong transferred = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                transferred.set(out.acceptEnergyFromNetwork(outputSide.getOpposite(), volt, amp));
            });
            if (!Objects.equals(AmpereMeterBE.this.unit, "A (%s)".formatted(Utils.voltagesMap.get(volt)))){
                AmpereMeterBE.this.average.clear();
                AmpereMeterBE.this.unit = "A (%s)".formatted(Utils.voltagesMap.get(volt));
            }
            if (AmpereMeterBE.this.average.size() >= 5){
                int trans = AmpereMeterBE.this.average.values().stream().reduce(0, Integer::sum)/AmpereMeterBE.this.average.size();
                AmpereMeterBE.this.transfer = Utils.shortenNumber(trans);
                AmpereMeterBE.this.numTransfer = trans;
                AmpereMeterBE.this.average.clear();
                if (AmpereMeterBE.this.getMenu() != null){
                    AmpereMeterBE.this.getMenu().unit = AmpereMeterBE.this.unit;
                    AmpereMeterBE.this.getMenu().transfer = AmpereMeterBE.this.transfer;
                }
            }
            AmpereMeterBE.this.average.put(AmpereMeterBE.this.average.size(), (int) transferred.get());
            return transferred.get();
        }
        @Override public boolean inputsEnergy(Direction direction) { return true; }
        @Override public long changeEnergy( long l ) { return 0; }
        @Override public long getEnergyStored() { return 0; }
        @Override public long getEnergyCapacity() { return Integer.MAX_VALUE; }
        @Override public long getInputAmperage() {
            if (AmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !AmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = AmpereMeterBE.this.getLevel().getBlockEntity(AmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong amperage = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                amperage.set(out.getInputAmperage());
            });
            return amperage.get();
        }
        @Override public long getInputVoltage() {
            if (AmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !AmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = AmpereMeterBE.this.getLevel().getBlockEntity(AmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong voltage = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                voltage.set(out.getInputVoltage());
            });
            return voltage.get();
        }
    };
}
