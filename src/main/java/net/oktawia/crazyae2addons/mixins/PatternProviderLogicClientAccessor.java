package net.oktawia.crazyae2addons.mixins;

import appeng.helpers.patternprovider.PatternProviderLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PatternProviderLogic.class, priority = 1400)
public interface PatternProviderLogicClientAccessor {
    @Invoker("failCraftingClient")
    void failCraftingClient(Object job);
}
