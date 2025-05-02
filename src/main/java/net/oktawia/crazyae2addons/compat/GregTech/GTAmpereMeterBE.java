package net.oktawia.crazyae2addons.compat.GregTech;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.entities.AmpereMeterBE;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class GTAmpereMeterBE extends AmpereMeterBE{

    public GTAmpereMeterBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
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

    public IEnergyContainer euLogic = new IEnergyContainer() {
        @Override public long acceptEnergyFromNetwork(Direction side, long volt, long amp) {
            if (GTAmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !GTAmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = GTAmpereMeterBE.this.getLevel().getBlockEntity(GTAmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong transferred = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                transferred.set(out.acceptEnergyFromNetwork(outputSide.getOpposite(), volt, amp));
            });
            if (!Objects.equals(GTAmpereMeterBE.this.unit, "A (%s)".formatted(Utils.voltagesMap.get(volt)))){
                GTAmpereMeterBE.this.average.clear();
                GTAmpereMeterBE.this.unit = "A (%s)".formatted(Utils.voltagesMap.get(volt));
            }
            if (GTAmpereMeterBE.this.average.size() >= 5){
                int trans = GTAmpereMeterBE.this.average.values().stream().reduce(0, Integer::sum)/ GTAmpereMeterBE.this.average.size();
                GTAmpereMeterBE.this.transfer = Utils.shortenNumber(trans);
                GTAmpereMeterBE.this.numTransfer = trans;
                GTAmpereMeterBE.this.average.clear();
                if (GTAmpereMeterBE.this.getMenu() != null){
                    GTAmpereMeterBE.this.getMenu().unit = GTAmpereMeterBE.this.unit;
                    GTAmpereMeterBE.this.getMenu().transfer = GTAmpereMeterBE.this.transfer;
                }
            }
            GTAmpereMeterBE.this.average.put(GTAmpereMeterBE.this.average.size(), (int) transferred.get());
            return transferred.get();
        }
        @Override public boolean inputsEnergy(Direction direction) { return true; }
        @Override public long changeEnergy( long l ) { return 0; }
        @Override public long getEnergyStored() { return 0; }
        @Override public long getEnergyCapacity() { return Integer.MAX_VALUE; }
        @Override public long getInputAmperage() {
            if (GTAmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !GTAmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = GTAmpereMeterBE.this.getLevel().getBlockEntity(GTAmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong amperage = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                amperage.set(out.getInputAmperage());
            });
            return amperage.get();
        }
        @Override public long getInputVoltage() {
            if (GTAmpereMeterBE.this.getLevel() == null) return 0;
            Direction outputSide = !GTAmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            BlockEntity output = GTAmpereMeterBE.this.getLevel().getBlockEntity(GTAmpereMeterBE.this.getBlockPos().relative(outputSide));
            if (output == null) return 0;
            AtomicLong voltage = new AtomicLong();
            output.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, outputSide.getOpposite()).ifPresent(out -> {
                voltage.set(out.getInputVoltage());
            });
            return voltage.get();
        }
    };


}
