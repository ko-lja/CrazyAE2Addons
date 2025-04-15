package net.oktawia.crazyae2addons.screens;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.ImpulsedPatternProviderMenu;

public class ImpulsedPatternProviderScreen<C extends ImpulsedPatternProviderMenu> extends AEBaseScreen<C> {

    private final SettingToggleButton<RedstoneMode> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;

    public ImpulsedPatternProviderScreen(C menu, Inventory playerInventory, Component title,
                                         ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.blockingModeButton = new SettingToggleButton<>(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, (a, b) -> {
        });
        this.addToLeftToolbar(this.blockingModeButton);
        this.lockCraftingModeButton = new SettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT, (a, b) -> {
        });
        this.addToLeftToolbar(lockCraftingModeButton);
        widgets.addOpenPriorityButton();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.blockingModeButton.set(this.getMenu().getBlockingMode());
        this.lockCraftingModeButton.set(this.getMenu().getLockCraftingMode());
    }
}
