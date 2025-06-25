package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.helpers.patternprovider.*;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.ConfigManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderCpu;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import net.oktawia.crazyae2addons.misc.PatternDetailsSerializer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = PatternProviderLogic.class, priority = 1100)
public abstract class MixinPatternProviderLogic implements IPatternProviderCpu {

    @Shadow @Final private PatternProviderLogicHost host;

    @Shadow
    @Final
    private IActionSource actionSource;

    @Unique
    private IPatternDetails patternDetails;

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
    @Override
    public void setPatternDetails(IPatternDetails details){
        this.patternDetails = details;
    }

    @Unique
    @Override
    public IPatternDetails getPatternDetails(){
        return this.patternDetails;
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

    @Unique
    private boolean ignoreNBT = false;

    @Shadow
    public abstract boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder);

    @Shadow public abstract boolean isClientSide();

    @Shadow @Nullable public abstract IGrid getGrid();

    @Shadow private Direction sendDirection;
    @Unique
    private YesNo realRedstoneState = YesNo.NO;

    @Inject(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At("RETURN"),
            remap = false
    )
    private void afterPushPatterns(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (host.getBlockEntity() != null && host.getBlockEntity().getType() == CrazyBlockEntityRegistrar.IMPULSED_PATTERN_PROVIDER_BE.get()){
            this.lastPattern = PatternDetailsSerializer.deserialize(PatternDetailsSerializer.serialize(patternDetails));
        }
        if (patternDetails.getDefinition().getTag() != null && patternDetails.getDefinition().getTag().contains("ignorenbt")){
            this.ignoreNBT = patternDetails.getDefinition().getTag().getBoolean("ignorenbt");
        } else {
            this.ignoreNBT = false;
        }
    }

    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("RETURN")
    )
    private void onCtorTail(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        if(host.getBlockEntity() != null && host.getBlockEntity().getType() == CrazyBlockEntityRegistrar.IMPULSED_PATTERN_PROVIDER_BE.get()) {
            this.configManager.putSetting(Settings.BLOCKING_MODE, YesNo.NO);
            this.configManager.putSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT);
        }
    }

    @Inject(
            method = "onStackReturnedToNetwork(Lappeng/api/stacks/GenericStack;)V",
            at = @At(
                    value  = "FIELD",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;unlockEvent:Lappeng/helpers/patternprovider/UnlockCraftingEvent;",
                    opcode = Opcodes.PUTFIELD,
                    shift  = At.Shift.AFTER
            ),
            remap = false
    )
    private void afterUnlockCleared(GenericStack genericStack, CallbackInfo ci) {
        if(host.getBlockEntity() != null && host.getBlockEntity().getType() == CrazyBlockEntityRegistrar.IMPULSED_PATTERN_PROVIDER_BE.get()){
            this.lastPattern = null;
            this.cpuCluster = null;
        }
    }

    @ModifyExpressionValue(
            method = "onStackReturnedToNetwork(Lappeng/api/stacks/GenericStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z"
            ),
            remap = false
    )
    private boolean modifyEquals(boolean originalCheck, GenericStack stack) {
        return (stack.what().getId() == this.unlockStack.what().getId() && this.ignoreNBT || originalCheck);
    }

    @Inject(
            method = "updateRedstoneState()V",
            at = @At("HEAD"),
            remap = false
    )
    private void beforeUpdateRedstoneState(CallbackInfo ci) {
        if(host.getBlockEntity() != null && host.getBlockEntity().getType() == CrazyBlockEntityRegistrar.IMPULSED_PATTERN_PROVIDER_BE.get()){
            if (realRedstoneState != YesNo.YES && getRealRedstoneState()){
                this.repeat();
            }
            realRedstoneState = getRealRedstoneState() ? YesNo.YES : YesNo.NO;
        }
    }

    @Inject(
            method = "writeToNBT(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN"),
            remap = false
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
        if (this.getPatternDetails() != null){
            tag.put("pdetails", PatternDetailsSerializer.serialize(this.getPatternDetails()));
        } else {
            tag.remove("pdetails");
        }
    }

    @Inject(
            method = "readFromNBT(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void afterReadFromNBT(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("lastpattern")){
            this.lastPattern = PatternDetailsSerializer.deserialize((CompoundTag) tag.get("lastpattern"));
        }
        if (tag.contains("cpucluster")){
            this.cpuClusterPos = BlockPos.of(tag.getLong("cpucluster"));
        }
        if (tag.contains("pdetails")){
            this.setPatternDetails(PatternDetailsSerializer.deserialize((CompoundTag) tag.get("pdetails")));
        }
    }

    @ModifyExpressionValue(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"
            ),
            remap = false
    )
    private boolean onPatternsContains(boolean originalResult, IPatternDetails pd) {
        if (pd.getClass() == PatternDetailsSerializer.PatternDetails.class) {
            return true;
        }
        return originalResult;
    }

    @ModifyExpressionValue(
            method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getCraftingLockedReason()Lappeng/api/config/LockCraftingMode;"
            ),
            remap = false
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

    @Inject(
            method = "findAdapter",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void redirectFind(Direction side, CallbackInfoReturnable<PatternProviderTarget> cir) {
        IPatternDetails pattern = this.getPatternDetails();
        if (pattern != null) {
            Object rawCache = getTargetCache(this, side.get3DDataValue());
            PatternProviderTarget result;
            if (rawCache instanceof IPatternProviderTargetCacheExt ext){
                result = ext.find(this.getPatternDetails());
                cir.setReturnValue(result);
            }
        }
    }

    @Unique
    private Object getTargetCache(Object logicInstance, int index) {
        try {
            Field f = logicInstance.getClass().getDeclaredField("targetCaches");
            f.setAccessible(true);
            Object[] caches = (Object[]) f.get(logicInstance);
            return caches[index];
        } catch (Exception e) {
            LogUtils.getLogger().info(e.toString());
            return null;
        }
    }

    @Unique
    public void repeat() {
        if (
                host.getBlockEntity() != null
                && this.cpuCluster == null
                && this.cpuClusterPos != null
                && host.getBlockEntity().getLevel() != null
                && host.getBlockEntity().getLevel().getBlockEntity(this.cpuClusterPos) != null
        ) {
            this.cpuCluster = ((CraftingBlockEntity) host.getBlockEntity()
                    .getLevel()
                    .getBlockEntity(this.cpuClusterPos))
                    .getCluster();
        }
        if (this.lastPattern != null) {
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
                    return;
                }
            }
            List<KeyCounter> holders = new ArrayList<>();
            for (var input : this.lastPattern.getInputs()) {
                KeyCounter holder = new KeyCounter();
                for (var item : input.getPossibleInputs()) {
                    long canExtract = Inv.extract(item.what(), item.amount(), Actionable.SIMULATE, this.actionSource);
                    if (canExtract >= item.amount()) {
                        holder.add(item.what(), item.amount());
                        break;
                    }
                }
                holders.add(holder);
            }
            KeyCounter[] inputHolderArray = holders.toArray(new KeyCounter[0]);
            boolean pushed = this.pushPattern(this.lastPattern, inputHolderArray);
            if (pushed) {
                for (KeyCounter holder : holders) {
                    holder.forEach((key) ->
                        Inv.extract(key.getKey(), key.getLongValue(), Actionable.MODULATE, this.actionSource)
                    );
                }
            }
        }
    }

    @Unique
    public void failCrafting(){
        if (this.cpuCluster != null) {
            this.cancelAndUnlock();
        }
    }

    @Unique
    private void cancelAndUnlock(){
        this.cpuCluster.cancelJob();
        this.unlockEvent = null;
        this.unlockStack = null;
        this.lastPattern = null;
        this.cpuCluster = null;
    }

    @Unique
    private boolean getRealRedstoneState() {
        if (host.getBlockEntity() != null){
            return host.getBlockEntity().getLevel().hasNeighborSignal(host.getBlockEntity().getBlockPos());
        } else {
            return false;
        }
    }
}