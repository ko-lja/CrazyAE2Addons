package net.oktawia.crazyae2addons.mixins;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.parts.misc.InterfacePart;
import appeng.parts.storagebus.StorageBusPart;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.defs.BlockEntities;
import net.oktawia.crazyae2addons.defs.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = PatternProviderLogic.class, priority = 1200, remap = false)
public abstract class MixinGT {

    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Shadow @Final
    private PatternProviderLogicHost host;

    @Inject(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getActiveSides()Ljava/util/Set;",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforePushToMachines(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (host.getBlockEntity().getType() == BlockEntities.CIRCUITED_PATTERN_PROVIDER_BE){
            onBeforeCraftingPush(patternDetails);
        }
    }

    @Unique
    private void onBeforeCraftingPush(IPatternDetails details) {
        CompoundTag tag = details.getDefinition().getTag();
        int circuit = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;

        var be = host.getBlockEntity();
        var level = be.getLevel();
        Set<BlockPos> visited = new HashSet<>();

        traverse(circuit, be.getBlockPos(), level, visited);
    }

    @Unique
    private void traverse(int circuit, BlockPos pos, Level level, Set<BlockPos> visited) {
        if (!visited.add(pos)) return;
        for (Direction dir : getActiveSides()) {
            BlockPos adjPos = pos.relative(dir);
            setCirc(circuit, adjPos, level);
            BlockEntity adjBe = level.getBlockEntity(adjPos);
            if (adjBe instanceof CableBusBlockEntity cbbe) {
                InterfacePart ip = getInterfacePart(cbbe, dir.getOpposite());
                if (ip != null) {
                    ip.getGridNode().getGrid()
                            .getMachines(StorageBusPart.class)
                            .forEach(bus -> {
                                if (bus.isUpgradedWith(Items.CIRCUIT_UPGRADE_CARD_ITEM)) {
                                    BlockEntity busBe = bus.getBlockEntity();
                                    Level busLevel = busBe.getLevel();
                                    BlockPos nextPos = busBe.getBlockPos().relative(bus.getSide());
                                    setCirc(circuit, nextPos, busLevel);
                                    traverse(circuit, nextPos, busLevel, visited);
                                }
                            });
                }
            }
        }
    }

    @Unique
    private InterfacePart getInterfacePart(CableBusBlockEntity cbbe, Direction side) {
        var part = cbbe.getPart(side);
        return (part instanceof InterfacePart) ? (InterfacePart) part : null;
    }

    @Unique
    private static void setCirc(int circ, BlockPos pos, Level lvl){
        var machine = SimpleTieredMachine.getMachine(lvl, pos);
        NotifiableItemStackHandler inv;
        if (machine instanceof SimpleTieredMachine STM){
            inv = STM.getCircuitInventory();
        } else if (machine instanceof ItemBusPartMachine IBPM) {
            inv = IBPM.getCircuitInventory();
        } else if (machine instanceof FluidHatchPartMachine FHPM) {
            inv = FHPM.getCircuitInventory();
        } else {
            return;
        }
        if (circ == 0){
            inv.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            var machineStack = GTItems.PROGRAMMED_CIRCUIT.asStack();
            IntCircuitBehaviour.setCircuitConfiguration(machineStack, circ);
            inv.setStackInSlot(0, machineStack);
        }
    }
}
