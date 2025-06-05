package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.YesNo;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.interfaces.IExclusivePatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PatternProviderScreen.class)
abstract public class MixinPatternProviderScreen {

    @Shadow @Final private SettingToggleButton<YesNo> blockingModeButton;
    @Unique private ToggleButton exclusiveModeButton;

    @Inject(
            method = "<init>(Lappeng/menu/implementations/PatternProviderMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;Lappeng/client/gui/style/ScreenStyle;)V",
            at = @At("RETURN")
    )
    private void atCtorTail(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci){
        this.exclusiveModeButton = new ToggleButton(Icon.VALID, Icon.INVALID, x ->
                ((IExclusivePatternProviderMenu)
                        ((AbstractContainerScreen<?>) (Object) this).getMenu()
                ).setExclusiveMode(x)
        );
        this.exclusiveModeButton.setTooltipOn(List.of(Component.literal("Exclusive Mode"),
                Component.literal("This pattern provider will not push to a machine that has already been used this tick")));
        this.exclusiveModeButton.setTooltipOff(List.of(Component.literal("Exclusive Mode"),
                Component.literal("This pattern provider will push even if the machine was already used this tick")));
        this.exclusiveModeButton.setVisibility(false);
        ((AEBaseScreenInvoker)this).invokeAddToLeftToolbar(this.exclusiveModeButton);
    }

    @Inject(
            method = "updateBeforeRender()V",
            at = @At("RETURN")
    )
    private void afterUpdateBeforeRender(CallbackInfo ci){
        if (((AbstractContainerScreen<?>) (Object) this).getMenu() instanceof IExclusivePatternProviderMenu eppm && eppm.getGridHasCraftingGuard() && this.blockingModeButton.getCurrentValue() == YesNo.YES){
            this.exclusiveModeButton.setVisibility(true);
            } else {
            this.exclusiveModeButton.setVisibility(false);
            ((IExclusivePatternProviderMenu) ((AbstractContainerScreen<?>) (Object) this).getMenu()).setExclusiveMode(false);
        }
        this.exclusiveModeButton.setState(((IExclusivePatternProviderMenu) ((AbstractContainerScreen<?>) (Object) this).getMenu()).getExclusiveMode());
    }
}
