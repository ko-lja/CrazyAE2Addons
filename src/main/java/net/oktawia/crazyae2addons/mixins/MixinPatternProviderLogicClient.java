package net.oktawia.crazyae2addons.mixins;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import net.minecraft.client.Minecraft;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderCpu;
import net.oktawia.crazyae2addons.misc.JobFailedToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PatternProviderLogic.class, priority = 1400, remap = false)
public abstract class MixinPatternProviderLogicClient implements PatternProviderLogicClientAccessor {

    @Shadow @Final
    private PatternProviderLogicHost host;

    @Unique
    public void failCraftingClient(Object job) {
        Minecraft.getInstance().getToasts().addToast(new JobFailedToast(((IPatternProviderCpu) this.host).getCpuCluster().getJobStatus().crafting().what()));
    }
}