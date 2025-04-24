package net.oktawia.crazyae2addons.screens;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.VerticalButtonBar;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.ImpulsedPatternProviderMenu;

import java.lang.reflect.Field;
import java.util.List;

public class ImpulsedPatternProviderScreen<C extends ImpulsedPatternProviderMenu> extends PatternProviderScreen<C> {
    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;

    public ImpulsedPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        try {
            Field toolbarField = AEBaseScreen.class.getDeclaredField("verticalToolbar");
            toolbarField.setAccessible(true);
            Object toolbarObj = toolbarField.get(this);
            VerticalButtonBar toolbar = (VerticalButtonBar) toolbarObj;
            Field buttonsField = VerticalButtonBar.class.getDeclaredField("buttons");
            buttonsField.setAccessible(true);
            List<Button> buttons = (List<Button>) buttonsField.get(toolbar);
            buttons.clear();
        } catch (Exception ignored) {}

        this.blockingModeButton = new SettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO, (x, y) -> {});
        this.addToLeftToolbar(this.blockingModeButton);

        lockCraftingModeButton = new SettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT, (x, y) -> {});
        this.addToLeftToolbar(lockCraftingModeButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.blockingModeButton.set(YesNo.NO);
        this.lockCraftingModeButton.set(this.getMenu().getLockCraftingMode());
    }
}


