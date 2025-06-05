package net.oktawia.crazyae2addons.mixins;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;
import net.oktawia.crazyae2addons.interfaces.IExclusivePatternProvider;
import net.oktawia.crazyae2addons.interfaces.IExclusivePatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderMenu.class)
abstract public class MixinPatternProviderMenu implements IExclusivePatternProviderMenu {
    @Shadow @Final protected PatternProviderLogic logic;
    @Unique private static String EXCLUSIVE_ACTION = "sendExclusiveMode";

    @Unique
    public void setExclusiveMode(boolean mode){
        ((IExclusivePatternProvider)this.logic).setExclusiveMode(mode);
        if (logic.isClientSide()){
            ((AEBaseMenuInvoker) this).invokeSendClientAction(EXCLUSIVE_ACTION, mode);
        }
    }

    @GuiSync(932)
    @Unique public boolean exclusiveMode = false;
    @GuiSync(933)
    @Unique public boolean gridHasCrafringGuard = false;

    @Unique public boolean getExclusiveMode(){
        return ((IExclusivePatternProvider)this.logic).getExclusiveMode();
    }

    @Unique public boolean getGridHasCraftingGuard(){
        return this.gridHasCrafringGuard;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("RETURN")
    )
    private void atCtorTail(MenuType menuType, int id, Inventory playerInventory, PatternProviderLogicHost host, CallbackInfo ci){
        ((AEBaseMenuInvoker) this).invokeRegisterClientAction(EXCLUSIVE_ACTION, Boolean.class, this::setExclusiveMode);
        var grid = ((PatternProviderMenuAccessor) this).getLogic().getGrid();
        if (grid != null) {
            var machine = grid.getMachines(CraftingGuardBE.class).stream().findFirst().orElse(null);
            if (machine != null) {
                this.gridHasCrafringGuard = true;
            }
        }
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getConfigManager()Lappeng/api/util/IConfigManager;",
                    ordinal = 0
            )
    )
    private void beforeBlockingModeUpdate(CallbackInfo ci) {
        this.exclusiveMode = ((IExclusivePatternProvider)this.logic).getExclusiveMode();
    }
}
