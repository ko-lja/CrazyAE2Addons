package net.oktawia.crazyae2addons.mixins;

import appeng.api.networking.security.IActionSource;
import appeng.helpers.patternprovider.PatternProviderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "appeng.helpers.patternprovider.PatternProviderTargetCache")
public interface PatternProviderTargetCacheAccessor {
    @Accessor("src")
    IActionSource getSrc();
    @Invoker("find")
    PatternProviderTarget callFind();
}