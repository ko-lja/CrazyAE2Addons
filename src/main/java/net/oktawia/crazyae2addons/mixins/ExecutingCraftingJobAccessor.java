package net.oktawia.crazyae2addons.mixins;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ExecutingCraftingJob.class)
public interface ExecutingCraftingJobAccessor {

    @Accessor("waitingFor")
    ListCraftingInventory getWaitingFor();

    @Accessor("timeTracker")
    ElapsedTimeTracker getTimeTracker();

    @Accessor("finalOutput")
    GenericStack getFinalOutput();

    @Accessor("link")
    CraftingLink getLink();

    @Accessor("remainingAmount")
    long getRemainingAmount();

    @Accessor("remainingAmount")
    void setRemainingAmount(long remainingAmount);
}