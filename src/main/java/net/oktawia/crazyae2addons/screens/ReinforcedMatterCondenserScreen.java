package net.oktawia.crazyae2addons.screens;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.CondenserMenu;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.AutoEnchanterMenu;
import net.oktawia.crazyae2addons.menus.ReinforcedMatterCondenserMenu;
import net.oktawia.crazyae2addons.misc.IconButton;

import java.util.List;

public class ReinforcedMatterCondenserScreen<C extends ReinforcedMatterCondenserMenu> extends AEBaseScreen<C> {

    public ReinforcedMatterCondenserScreen(C menu, Inventory playerInventory, Component title,
                           ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.add("progressBar", new ProgressBar(this.menu, style.getImage("progressBar"),
                ProgressBar.Direction.VERTICAL, Component.literal("Stored singularities")));
        widgets.add("progressBar2", new ProgressBar(this.menu.CellProvider, style.getImage("progressBar"),
                ProgressBar.Direction.VERTICAL, Component.literal("Stored storage cells")));
    }
}