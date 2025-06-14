package net.oktawia.crazyae2addons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.Slot;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("x")
    void setX(int x);

    @Accessor("y")
    void setY(int y);
}
