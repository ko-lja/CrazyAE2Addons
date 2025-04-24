package net.oktawia.crazyae2addons.mixins;

import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PatternProviderLogic.class)
public interface PatternProviderLogicAccessor {
    @Invoker("adapterAcceptsAll")
    boolean callAdapterAcceptsAll(PatternProviderTarget adapter, KeyCounter[] inputHolder);
}