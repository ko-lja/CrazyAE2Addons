package net.oktawia.crazyae2addons.mixins;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;
import net.oktawia.crazyae2addons.interfaces.IExclusivePatternProvider;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderCpu;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stone.mae2.appeng.helpers.patternprovider.PatternProviderTargetCache;
import stone.mae2.parts.p2p.PatternP2PTunnel;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(value = PatternProviderLogic.class)
public abstract class MixinMAE2 implements IPatternProviderCpu, IExclusivePatternProvider {

    @Unique
    private IPatternDetails patternDetails;

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
    private boolean exclusiveMode = false;

    @Unique
    public void setExclusiveMode(boolean mode){
        this.exclusiveMode = mode;
    }

    @Unique
    public boolean getExclusiveMode(){
        return this.exclusiveMode;
    }

    @Shadow
    @Nullable
    public abstract IGrid getGrid();

    @Inject(
            method = "findAdapters",
            at = @At("RETURN")
    )
    private void injectAfterFindAdapters(BlockEntity be, Level level, List<PatternP2PTunnel.TunneledPatternProviderTarget> adapters, Direction direction, CallbackInfo ci) {
        var pattern = this.getPatternDetails();
        if (pattern == null) return;
        for (int i = 0; i < adapters.size(); i++) {
            var adapter = adapters.get(i);
            var target = adapter.target();
            if (target instanceof IPatternProviderTargetCacheExt ext) {
                ext.setExclusiveMode(this.getExclusiveMode());
                ext.setDetails(pattern);
                var grid = this.getGrid();
                if (grid != null){
                    var guard = grid.getMachines(CraftingGuardBE.class).stream().findFirst().orElse(null);
                    if (guard != null){
                        ext.setGuard(guard);
                    }
                }
            }
        }
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
                ext.setExclusiveMode(this.getExclusiveMode());
                var grid = this.getGrid();
                if (grid != null){
                    var guard = grid.getMachines(CraftingGuardBE.class).stream().findFirst().orElse(null);
                    if (guard != null){
                        ext.setGuard(guard);
                    }
                }
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
}