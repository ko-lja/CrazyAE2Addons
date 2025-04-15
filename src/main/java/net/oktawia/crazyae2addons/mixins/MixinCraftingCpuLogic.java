package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.*;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.logging.LogUtils;
import net.oktawia.crazyae2addons.interfaces.IIgnoreNBT;
import net.oktawia.crazyae2addons.logic.Impulsed.ImpulsedPatternProviderLogic;
import net.oktawia.crazyae2addons.logic.Impulsed.ImpulsedPatternProviderLogicHost;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingCpuLogic.class)
public abstract class MixinCraftingCpuLogic {

    @Shadow
    private ExecutingCraftingJob job;

    @Shadow public abstract void cancel();

    @Shadow @Final private CraftingCPUCluster cluster;
    @Unique
    public boolean ignoreNBT = false;


    @Inject(method = "trySubmitJob", at = @At("TAIL"), remap = false)
    public void trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src, ICraftingRequester requester, CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        plan.patternTimes().forEach((pattern, ignored) -> {
            if (pattern.getPrimaryOutput().what().matches(plan.finalOutput())) {
                ignoreNBT = pattern.getDefinition().getTag().getBoolean("ignorenbt");
            }
        });
        ((IIgnoreNBT) ((ExecutingCraftingJobAccessor) job).getWaitingFor()).setIgnoreNBT(ignoreNBT);
    }

    @ModifyExpressionValue(
            method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/stacks/AEKey;matches(Lappeng/api/stacks/GenericStack;)Z"
            )
    )
    private boolean modifyFinalOutputCheck(boolean originalCheck, AEKey what, long amount, Actionable type) {
        return ((what.fuzzyEquals(((ExecutingCraftingJobAccessor) job).getFinalOutput().what(), FuzzyMode.IGNORE_ALL)) && this.ignoreNBT || originalCheck);
    }

    @Redirect(
            method = "executeCrafting",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"
            ),
            remap = false
    )
    private boolean redirectPushPattern(ICraftingProvider instance, IPatternDetails patternDetails, KeyCounter[] keyCounters) {
        if (instance instanceof ImpulsedPatternProviderLogic impulsedLogic){
            return impulsedLogic.pushPattern(patternDetails, keyCounters, this.cluster);
        }
        return instance.pushPattern(patternDetails, keyCounters);
    }
}