package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(targets = "appeng.helpers.patternprovider.PatternProviderTargetCache")
public abstract class MixinPatternProviderTargetCache implements IPatternProviderTargetCacheExt {

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
    public PatternProviderTarget find(IPatternDetails patternDetails) {
        this.details = patternDetails;
        return ((PatternProviderTargetCacheAccessor) this).callFind();
    }

    @Inject(
            method = "wrapMeStorage(Lappeng/api/storage/MEStorage;)Lappeng/helpers/patternprovider/PatternProviderTarget;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void injectWrapMeStorage(MEStorage storage, CallbackInfoReturnable<PatternProviderTarget> cir) {
        var self = (PatternProviderTargetCacheAccessor) this;
        IActionSource src = self.getSrc();

        cir.setReturnValue(new PatternProviderTarget() {
            private final BlockPos pos1 = MixinPatternProviderTargetCache.this.pos;
            private final Level lvl1 = MixinPatternProviderTargetCache.this.lvl;
            private final CraftingGuardBE guard1 = MixinPatternProviderTargetCache.this.guard;
            private final boolean exclusiveMode1 = MixinPatternProviderTargetCache.this.exclusiveMode;
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                var result = storage.insert(what, amount, type, src);
                if (this.guard1 != null && result > 0 && this.guard1.getLevel() != null && this.guard1.getLevel().getServer() != null){
                    this.guard1.excluded.put(this.pos1, this.guard1.getLevel().getServer().getTickCount());
                }
                return result;
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) {
                        return true;
                    }
                }
                var server = this.lvl1.getServer();
                if (server != null && this.guard1 != null && this.guard1.excluded.get(this.pos1) != null){
                    return this.guard1.excluded.get(this.pos1) == server.getTickCount() && this.exclusiveMode1;
                }
                return false;
            }
        });
    }
}