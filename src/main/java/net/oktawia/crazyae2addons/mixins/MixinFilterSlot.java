package net.oktawia.crazyae2addons.mixins;

import appeng.api.stacks.GenericStack;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = appeng.menu.slot.FakeSlot.class, priority = 1100)
public abstract class MixinFilterSlot extends Slot {

    public MixinFilterSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Inject(
        method = "set(Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onSet(ItemStack is, CallbackInfo ci) {
        if (is.getItem() instanceof SpawnEggItem egg) {
            MobKey key = MobKey.of(egg.getType(is.getTag()));
            GenericStack ghost = new GenericStack(key, 1L);
            super.set(GenericStack.wrapInItemStack(ghost));
            ci.cancel();
        }
    }
}