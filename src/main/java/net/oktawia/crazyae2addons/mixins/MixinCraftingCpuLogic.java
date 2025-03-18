package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.execution.*;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.mojang.logging.LogUtils;
import net.oktawia.crazyae2addons.interfaces.IIgnoreNBT;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingCpuLogic.class)
public abstract class MixinCraftingCpuLogic {

    @Shadow
    private ExecutingCraftingJob job;

    @Final
    @Shadow
    CraftingCPUCluster cluster;

    @Final
    @Shadow
    private ListCraftingInventory inventory;

    @Shadow
    protected abstract void postChange(AEKey what);

    @Shadow
    protected abstract void finishJob(boolean success);

    @Unique
    public boolean ignoreNBT = false;


    /**
     * @author oktawia
     * @reason add ignore nbt
     */
    @Inject(method = "trySubmitJob", at = @At("TAIL"), remap = false)
    public void trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src, ICraftingRequester requester, CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        plan.patternTimes().forEach((pattern, ignored) -> {
            if (pattern.getPrimaryOutput().what().matches(plan.finalOutput())) {
                ignoreNBT = pattern.getDefinition().getTag().getBoolean("ignorenbt");
            }
        });
        ((IIgnoreNBT) ((ExecutingCraftingJobAccessor) job).getWaitingFor()).setIgnoreNBT(ignoreNBT);
        LogUtils.getLogger().info(String.valueOf(((IIgnoreNBT) ((ExecutingCraftingJobAccessor) job).getWaitingFor()).getIgnoreNBT()));
    }

    /**
     * @author oktawia
     * @reason add ignore nbt option
     */
    @Overwrite(remap = false)
    public long insert(AEKey what, long amount, Actionable type) {
        if (what == null || job == null)
            return 0;

        var waitingFor = ((ExecutingCraftingJobAccessor) job).getWaitingFor().extract(what, amount, Actionable.SIMULATE);
        if (waitingFor <= 0) {
            return 0;
        }
        if (amount > waitingFor) {
            amount = waitingFor;
        }
        if (type == Actionable.MODULATE) {
            ElapsedTimeTracker tracker = ((ExecutingCraftingJobAccessor) job).getTimeTracker();
            ((ElapsedTimeTrackerAccessor) tracker).invokeDecrementItems(amount, what.getType());
            ((ExecutingCraftingJobAccessor) job).getWaitingFor().extract(what, amount, Actionable.MODULATE);
            cluster.markDirty();
        }
        long inserted = amount;
        if ((what.fuzzyEquals(((ExecutingCraftingJobAccessor) job).getFinalOutput().what(), FuzzyMode.IGNORE_ALL)) && this.ignoreNBT
                || what.matches(((ExecutingCraftingJobAccessor) job).getFinalOutput())) {
            inserted = ((ExecutingCraftingJobAccessor) job).getLink().insert(what, amount, type);
            if (type == Actionable.MODULATE) {
                postChange(what);
                ((ExecutingCraftingJobAccessor) job).setRemainingAmount(Math.max(0, ((ExecutingCraftingJobAccessor) job).getRemainingAmount() - amount));
                if (((ExecutingCraftingJobAccessor) job).getRemainingAmount() <= 0) {
                    finishJob(true);
                    cluster.updateOutput(null);
                } else {
                    cluster.updateOutput(new GenericStack(((ExecutingCraftingJobAccessor) job).getFinalOutput().what(), ((ExecutingCraftingJobAccessor) job).getRemainingAmount()));
                }
            }
        } else if (type == Actionable.MODULATE) {
            inventory.insert(what, amount, Actionable.MODULATE);
        }
        return inserted;
    }
}