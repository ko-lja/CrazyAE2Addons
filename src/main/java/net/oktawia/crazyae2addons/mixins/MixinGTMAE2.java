package net.oktawia.crazyae2addons.mixins;

import appeng.api.crafting.IPatternDetails;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
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
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stone.mae2.parts.p2p.PatternP2PTunnel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = PatternProviderLogic.class, priority = 1300, remap = false)
public abstract class MixinGTMAE2 {

    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Inject(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getActiveSides()Ljava/util/Set;",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforePushToMachines(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("gtceu") && host.getBlockEntity().getType() == CrazyBlockEntityRegistrar.CIRCUITED_PATTERN_PROVIDER_BE.get()){
            mae2SetCirc(patternDetails);
        }
    }

    @Unique
    private void mae2SetCirc(IPatternDetails details) {
        CompoundTag tag = details.getDefinition().getTag();
        int circuit = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;
        BlockEntity be = host.getBlockEntity();
        Level level = be.getLevel();
        Set<BlockPos> visited = new HashSet<>();

        for (Direction dir : getActiveSides()) {
            BlockPos start = be.getBlockPos().relative(dir);
            traverseTunneled(circuit, start, level, dir.getOpposite(), visited);
        }
    }

    @Unique
    private void traverseTunneled(int circuit, BlockPos pos, Level level, Direction entrySide, Set<BlockPos> visited) {
        if (!visited.add(pos)) return;
        setCirc(circuit, pos, level);

        List<PatternP2PTunnel.TunneledPos> tunnels = getTunneledPositions(pos, level, entrySide);
        if (tunnels == null) return;

        for (PatternP2PTunnel.TunneledPos tp : tunnels) {
            BlockPos tpPos = tp.pos();
            setCirc(circuit, tpPos, level);
            BlockEntity adjBe = level.getBlockEntity(tpPos);

            if (adjBe instanceof CableBusBlockEntity cbbe) {
                InterfacePart ip = getInterfacePart(cbbe, entrySide);
                if (ip != null) {
                    ip.getGridNode().getGrid()
                            .getMachines(StorageBusPart.class)
                            .forEach(bus -> {
                                if (bus.isUpgradedWith(CrazyItemRegistrar.CIRCUIT_UPGRADE_CARD_ITEM.get())) {
                                    BlockEntity busBe = bus.getBlockEntity();
                                    Level busLevel = busBe.getLevel();
                                    BlockPos next = busBe.getBlockPos().relative(bus.getSide());
                                    traverseTunneled(circuit, next, busLevel, bus.getSide().getOpposite(), visited);
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
    private List<PatternP2PTunnel.TunneledPos> getTunneledPositions(BlockPos pos, Level level, Direction adjBeSide) {
        BlockEntity potentialPart = level.getBlockEntity(pos);
        if (potentialPart != null && potentialPart instanceof IPartHost) {
            IPart potentialTunnel = ((IPartHost)potentialPart).getPart(adjBeSide);
            if (potentialTunnel instanceof PatternP2PTunnel) {
                PatternP2PTunnel tunnel = (PatternP2PTunnel)potentialTunnel;
                return tunnel.getTunneledPositions();
            } else {
                return List.of(new PatternP2PTunnel.TunneledPos(pos, adjBeSide));
            }
        } else {
            return List.of(new PatternP2PTunnel.TunneledPos(pos, adjBeSide));
        }
    }

    @Unique
    private static void setCirc(int circ, BlockPos pos, Level lvl){
        if (!CrazyConfig.COMMON.enableCPP.get()) return;
        try {
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
            if (inv.getSlots() == 0) {
                return;
            }
            if (circ == 0){
                inv.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                var machineStack = GTItems.PROGRAMMED_CIRCUIT.asStack();
                IntCircuitBehaviour.setCircuitConfiguration(machineStack, circ);
                inv.setStackInSlot(0, machineStack);
            }
        } catch (Throwable e) {
            LogUtils.getLogger().info(e.toString());
        }
    }
}
