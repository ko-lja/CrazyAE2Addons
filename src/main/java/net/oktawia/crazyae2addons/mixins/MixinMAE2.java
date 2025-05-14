package net.oktawia.crazyae2addons.mixins;


import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.interfaces.IPatternProviderTargetCacheExt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stone.mae2.appeng.helpers.patternprovider.PatternProviderTargetCache;

import java.util.Set;

@Mixin(value = PatternProviderTargetCache.class, remap = false)
public abstract class MixinMAE2 implements IPatternProviderTargetCacheExt {

    @Shadow public abstract @Nullable PatternProviderTarget find();

    @Shadow @Final private IActionSource src;
    @Unique
    private BlockPos pos = null;
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
    public void setDetails(IPatternDetails patternDetails) {
        this.details = patternDetails;
    }

    @ModifyReturnValue(
            method = "wrapMeStorage(Lappeng/api/storage/MEStorage;)Lappeng/helpers/patternprovider/PatternProviderTarget;",
            at = @At("RETURN"),
            remap = false
    )
    private PatternProviderTarget injectWrapMeStorage(PatternProviderTarget original, MEStorage storage) {
        var self = this;
        IActionSource src = self.src;

        return new PatternProviderTarget() {
            private final BlockPos pos1 = MixinMAE2.this.pos;
            private final Level lvl1 = MixinMAE2.this.lvl;
            private final IPatternDetails details1 = MixinMAE2.this.details;
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                if (details1 != null){
                    CompoundTag tag = details1.getDefinition().getTag();
                    int c = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;
                    setCirc(c, pos1, lvl1);
                }
                return storage.insert(what, amount, type, src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    @Unique
    private static void setCirc(int circ, BlockPos pos, Level lvl){
        if (!CrazyConfig.COMMON.enableCPP.get()) return;
        try {
            var machine = SimpleTieredMachine.getMachine(lvl, pos);
            NotifiableItemStackHandler inv;
            if (machine instanceof SimpleTieredMachine STM){
                inv = STM.getCircuitInventory();
            } else if (machine instanceof ItemBusPartMachine IBPM) {
                inv = IBPM.getCircuitInventory();
            } else if (machine instanceof FluidHatchPartMachine FHPM) {
                inv = FHPM.getCircuitInventory();
            } else {
                return;
            }
            if (circ == 0){
                inv.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                var machineStack = GTItems.PROGRAMMED_CIRCUIT.asStack();
                IntCircuitBehaviour.setCircuitConfiguration(machineStack, circ);
                inv.setStackInSlot(0, machineStack);
            }
        } catch (Exception e){
            LogUtils.getLogger().info(e.toString());
        }
    }
}