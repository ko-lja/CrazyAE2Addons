package net.oktawia.crazyae2addons.mixins;

import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(AEBaseMenu.class)
public interface AEBaseMenuInvoker {
    @Invoker("sendClientAction")
    <T> void invokeSendClientAction(String action, T arg);
    @Invoker("registerClientAction")
    <T> void invokeRegisterClientAction(String action, Class<T> argClass, Consumer<T> handler);
}