package net.oktawia.crazyae2addons.mixins;

import appeng.client.gui.AEBaseScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseScreen.class)
public interface AEBaseScreenInvoker {
    @Invoker("addToLeftToolbar")
    <B extends Button> B invokeAddToLeftToolbar(B button);
}