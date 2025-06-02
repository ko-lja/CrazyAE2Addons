package net.oktawia.crazyae2addons.mixins;

import appeng.parts.reporting.AbstractMonitorPart;
import com.mojang.logging.LogUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = appeng.parts.reporting.AbstractMonitorPart.class)
public abstract class MixinAbstractMonitorPart {
    @Inject(
            method = "onPartActivate(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/behaviors/ContainerItemStrategies;getContainedStack(Lnet/minecraft/world/item/ItemStack;)Lappeng/api/stacks/GenericStack;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onGetContainedStack(Player player, InteractionHand hand, Vec3 pos, CallbackInfoReturnable<Boolean> cir, ItemStack eq) {
        if (eq.getItem() instanceof SpawnEggItem egg) {
            MobKey key = MobKey.of(egg.getType(eq.getTag()));
            ((AbstractMonitorPart) (Object) this).setConfiguredItem(key);
        }
    }
}