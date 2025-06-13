package net.oktawia.crazyae2addons.mixins;


import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stone.mae2.appeng.helpers.patternprovider.PatternProviderTargetCache;

import java.util.Set;

@Mixin(value = PatternProviderTargetCache.class, priority = 910)
public abstract class MixinMAE22 implements IPatternProviderTargetCacheExt {

    @Shadow public abstract @Nullable PatternProviderTarget find();

    @Shadow @Final private IActionSource src;
    @Shadow @Final private Direction direction;
    @Unique
    private BlockPos pos = null;
    @Unique private Level lvl = null;
    @Unique private IPatternDetails details = null;
    @Unique private CraftingGuardBE guard = null;
    @Unique private boolean exclusiveMode = false;

    @Unique public void setGuard(CraftingGuardBE guard){
        this.guard = guard;
    }

    @Unique public void setExclusiveMode(boolean mode){
        this.exclusiveMode = mode;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lappeng/api/networking/security/IActionSource;)V",
            at = @At("RETURN")
    )
    private void atCtorTail(ServerLevel l, BlockPos pos, Direction direction, IActionSource src, CallbackInfo ci){
        this.pos = pos;
        this.lvl = l;
    }

    @Unique
    public void setDetails(IPatternDetails patternDetails) {
        this.details = patternDetails;
    }

    @Unique
    public PatternProviderTarget find(IPatternDetails patternDetails) {
        this.details = patternDetails;
        PatternProviderTarget original = this.find();
        if (original == null) return null;

        return new PatternProviderTarget() {
            private final IPatternDetails details1 = MixinMAE22.this.details;
            private final CraftingGuardBE guard1    = MixinMAE22.this.guard;
            private final boolean exclusiveMode1    = MixinMAE22.this.exclusiveMode;
            private final BlockPos pos1             = MixinMAE22.this.pos;
            private final Level lvl1                = MixinMAE22.this.lvl;

            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                var result = original.insert(what, amount, type);
                if (this.guard1 != null && result > 0 && this.guard1.getLevel() != null && this.guard1.getLevel().getServer() != null){
                    this.guard1.excluded.put(this.pos1, this.guard1.getLevel().getServer().getTickCount());
                }
                return result;
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                return original.containsPatternInput(patternInputs)
                        || (guard1 != null
                        && guard1.excluded.get(pos1) != null
                        && guard1.excluded.get(pos1).equals(lvl1.getServer().getTickCount())
                        && exclusiveMode1);
            }
        };
    }

}