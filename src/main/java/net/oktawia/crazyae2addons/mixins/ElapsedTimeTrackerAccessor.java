package net.oktawia.crazyae2addons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.api.stacks.AEKeyType;

@Mixin(ElapsedTimeTracker.class)
public interface ElapsedTimeTrackerAccessor {

    @Invoker("decrementItems")
    void invokeDecrementItems(long amount, AEKeyType keyType);
}
