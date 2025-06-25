package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.helpers.patternprovider.PatternProviderTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stone.mae2.appeng.helpers.patternprovider.PatternProviderTargetCache;

import java.util.Set;

@Mixin(value = PatternProviderTargetCache.class, priority = 910)
public abstract class MixinMAE22 implements IPatternProviderTargetCacheExt {

    @Shadow public abstract @Nullable PatternProviderTarget find();

    @Unique private BlockPos pos = null;
    @Unique private Level lvl = null;
    @Unique private IPatternDetails details = null;

    @Inject(
            method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lappeng/api/networking/security/IActionSource;)V",
            at = @At("RETURN")
    )
    private void atCtorTail(ServerLevel l, BlockPos pos, Direction direction, IActionSource src, CallbackInfo ci){
        this.pos = pos;
        this.lvl = l;
    }

    @Unique
    public void setDetails(IPatternDetails details){
        this.details = details;
    }

    @Unique
    public PatternProviderTarget find(IPatternDetails patternDetails) {
        PatternProviderTarget original = this.find();
        if (original == null) return null;

        return new PatternProviderTarget() {
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return original.insert(what, amount, type);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                return original.containsPatternInput(patternInputs);
            }
        };
    }
}