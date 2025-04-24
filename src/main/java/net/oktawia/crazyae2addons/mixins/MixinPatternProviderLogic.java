package net.oktawia.crazyae2addons.mixins;


import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.helpers.patternprovider.*;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.misc.InterfacePart;
import appeng.parts.storagebus.StorageBusPart;
import appeng.util.BlockApiCache;
import appeng.util.ConfigManager;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.defs.BlockEntities;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderCpu;
import net.oktawia.crazyae2addons.misc.JobFailedToast;
import net.oktawia.crazyae2addons.misc.KeyCounterSerializer;
import net.oktawia.crazyae2addons.misc.PatternDetailsSerializer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static appeng.crafting.execution.CraftingCpuHelper.extractPatternInputs;

@Mixin(PatternProviderLogic.class)
public abstract class MixinPatternProviderLogic implements IPatternProviderCpu {

    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Final
    @Shadow
    private List<GenericStack> sendList;
    @Shadow
    @Final
    private IManagedGridNode mainNode;

    @Shadow
    protected abstract void addToSendList(AEKey what, long amount);

    @Shadow
    protected abstract boolean sendStacksOut();

    @Shadow
    protected abstract PatternProviderTarget findAdapter(Direction side);

    @Shadow @Final private PatternProviderLogicHost host;

    @Shadow
    @Final
    private IActionSource actionSource;

    @Unique
    private CraftingCPUCluster cpuCluster = null;

    @Unique
    @Override
    public void setCpuCluster(CraftingCPUCluster cpu) {
        this.cpuCluster = cpu;
    }

    @Unique
    @Override
    public CraftingCPUCluster getCpuCluster() {
        return this.cpuCluster;
    }

    @Unique
    private IPatternDetails lastPattern = null;
    @Unique
    private BlockPos cpuClusterPos = null;

    @Shadow
    @Nullable
    private UnlockCraftingEvent unlockEvent;
    @Shadow
    @Nullable
    private GenericStack unlockStack;

    @Shadow
    @Final
    private ConfigManager configManager;

    @Shadow
    public abstract boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder);

    @Shadow private YesNo redstoneState;

    @Shadow public abstract boolean isClientSide();

    @Shadow @Final private List<IPatternDetails> patterns;
    @Unique
    private YesNo realRedstoneState = YesNo.NO;

    @Inject(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getActiveSides()Ljava/util/Set;",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforePushToMachines(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("gtceu") && host.getBlockEntity().getType() == BlockEntities.CIRCUITED_PATTERN_PROVIDER_BE){
            onBeforeCraftingPush(patternDetails, getActiveSides());
        }
    }

    @Inject(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At("RETURN")
    )
    private void afterPushPatterns(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (host.getBlockEntity().getType() == BlockEntities.IMPULSED_PATTERN_PROVIDER_BE){
            this.lastPattern = patternDetails;
        }
    }

    @Inject(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL")
    )
    private void onCtorTail(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        if(host.getBlockEntity().getType() == BlockEntities.IMPULSED_PATTERN_PROVIDER_BE) {
            this.configManager.putSetting(Settings.BLOCKING_MODE, YesNo.NO);
            this.configManager.putSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT);
        }
    }

    @Inject(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;onStackReturnedToNetwork(Lappeng/api/stacks/GenericStack;)V",
            at = @At(
                    value  = "FIELD",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;unlockEvent:Lappeng/helpers/patternprovider/UnlockCraftingEvent;",
                    opcode = Opcodes.PUTFIELD,
                    shift  = At.Shift.AFTER
            )
    )
    private void afterUnlockCleared(GenericStack genericStack, CallbackInfo ci) {
        if(host.getBlockEntity().getType() == BlockEntities.IMPULSED_PATTERN_PROVIDER_BE){
            this.lastPattern = null;
            this.cpuCluster = null;
        }
    }

    @Inject(
            method = "updateRedstoneState()V",
            at = @At("HEAD")
    )
    private void beforeUpdateRedstoneState(CallbackInfo ci) {
        if(host.getBlockEntity().getType() == BlockEntities.IMPULSED_PATTERN_PROVIDER_BE){
            if (realRedstoneState != YesNo.YES && getRealRedstoneState()){
                this.repeat();
            }
            realRedstoneState = getRealRedstoneState() ? YesNo.YES : YesNo.NO;
        }
    }

    @Inject(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;writeToNBT(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN")
    )
    private void afterWriteToNBT(CompoundTag tag, CallbackInfo ci) {
        if (this.lastPattern != null){
            tag.put("lastpattern", PatternDetailsSerializer.serialize(this.lastPattern));
        } else {
            tag.remove("lastpattern");
        }
        if (this.cpuCluster != null){
            BlockEntity cpuPart = this.cpuCluster.getBlockEntities().next();
            tag.putLong("cpucluster", cpuPart.getBlockPos().asLong());
        } else {
            tag.remove("cpucluster");
        }
    }

    @Inject(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;readFromNBT(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN")
    )
    private void afterReadFromNBT(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("lastpattern")){
            this.lastPattern = PatternDetailsSerializer.deserialize((CompoundTag) tag.get("lastpattern"));
        }
        if (tag.contains("cpucluster")){
            this.cpuClusterPos = BlockPos.of(tag.getLong("cpucluster"));
        }
    }

    @ModifyExpressionValue(
            method = "Lappeng/helpers/patternprovider/PatternProviderLogic;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"
            )
    )
    private boolean onPatternsContains(boolean originalResult, IPatternDetails pd) {
        if (pd instanceof PatternDetailsSerializer.PatternDetails) {
            return true;
        }
        return originalResult;
    }

    @Unique
    public void repeat(){
        if (this.cpuCluster == null && this.cpuClusterPos != null){
            this.cpuCluster = ((CraftingBlockEntity) host.getBlockEntity().getLevel().getBlockEntity(this.cpuClusterPos)).getCluster();
        }
        if (this.lastPattern != null){
            var Inv = host.getGrid().getStorageService().getInventory();
            for (var input : this.lastPattern.getInputs()) {
                boolean canSatisfy = false;
                for (var item : input.getPossibleInputs()) {
                    long extracted = Inv.extract(item.what(), item.amount(), Actionable.SIMULATE, this.actionSource);
                    if (extracted >= item.amount()) {
                        canSatisfy = true;
                        break;
                    }
                }
                if (!canSatisfy) {
                    failCrafting();
                }
            }
            List<KeyCounter> holders = new ArrayList<>();
            for (var input : this.lastPattern.getInputs()) {
                KeyCounter holder = new KeyCounter();
                for (var item : input.getPossibleInputs()) {
                    long canExtract = Inv.extract(item.what(), item.amount(), Actionable.SIMULATE, this.actionSource);
                    if (canExtract >= item.amount()) {
                        Inv.extract(item.what(), item.amount(), Actionable.MODULATE, this.actionSource);
                        holder.add(item.what(), item.amount());
                        break;
                    }
                }
                holders.add(holder);
            }
            KeyCounter[] inputHolderArray = holders.toArray(new KeyCounter[0]);
            this.pushPattern(this.lastPattern, inputHolderArray);
        }
    }

    @ModifyExpressionValue(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;"
                            + "getCraftingLockedReason()Lappeng/api/config/LockCraftingMode;"
            )
    )
    private LockCraftingMode onLockReason(
            LockCraftingMode original,
            IPatternDetails patternDetails
    ) {
        if (patternDetails instanceof PatternDetailsSerializer.PatternDetails) {
            return LockCraftingMode.NONE;
        }
        return original;
    }

    @Unique
    public void failCrafting(){
        Minecraft.getInstance().getToasts().addToast(new JobFailedToast(this.cpuCluster.getJobStatus().crafting().what()));
        this.cpuCluster.cancelJob();
        this.unlockEvent = null;
        this.unlockStack = null;
        this.lastPattern = null;
        this.cpuCluster = null;
    }

    @Unique
    private void onBeforeCraftingPush(IPatternDetails details, Set<Direction> activeSides) {
        CompoundTag tag = details.getDefinition().getTag();
        int circuit = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;

        var be = host.getBlockEntity();
        var level = be.getLevel();

        for (var direction : getActiveSides()) {
            var adjPos = be.getBlockPos().relative(direction);
            var adjBe = level.getBlockEntity(adjPos);
            setCirc(circuit, adjPos, level);
            if (adjBe instanceof CableBusBlockEntity cbbe) {
                var part = cbbe.getPart(direction.getOpposite());
                if (part instanceof InterfacePart ipart) {
                    ipart.getGridNode().getGrid()
                        .getMachines(StorageBusPart.class)
                        .forEach(bus -> {
                            if (bus.isUpgradedWith(Items.CIRCUIT_UPGRADE_CARD_ITEM)) {
                                setCirc(circuit, bus.getBlockEntity().getBlockPos().relative(bus.getSide()), bus.getLevel());
                            }
                        });
                }
            }
        }
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

    @Unique
    private boolean getRealRedstoneState() {
        var be = this.host.getBlockEntity();
        return be.getLevel().hasNeighborSignal(be.getBlockPos());
    }
}