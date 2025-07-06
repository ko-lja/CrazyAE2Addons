package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.AutoBuilderMenu;
import net.oktawia.crazyae2addons.misc.IconButton;

import java.util.Objects;

public class AutoBuilderScreen<C extends AutoBuilderMenu> extends UpgradeableScreen<C> {

    private final AETextField xlabel;
    private final AETextField ylabel;
    private final AETextField zlabel;
    private boolean initialized = false;

    public AutoBuilderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        var skipMissing = new AECheckbox(0,0,0,0, style, Component.literal("Skip"));
        skipMissing.setTooltip(Tooltip.create(Component.literal("Start building even if not all blocks are available")));
        skipMissing.setChangeListener(() -> {
            getMenu().updateMissing(skipMissing.isSelected());
        });
        skipMissing.setSelected(getMenu().skipEmpty);
        var north = new IconButton(Icon.ARROW_UP, btn -> changex(1));
        north.setTooltip(Tooltip.create(Component.literal("Add 1 offset to north")));
        var south = new IconButton(Icon.ARROW_DOWN, btn -> changex(-1));
        south.setTooltip(Tooltip.create(Component.literal("Add 1 offset to south")));
        var east = new IconButton(Icon.ARROW_UP, btn -> changez(1));
        east.setTooltip(Tooltip.create(Component.literal("Add 1 offset to east")));
        var west = new IconButton(Icon.ARROW_DOWN, btn -> changez(-1));
        west.setTooltip(Tooltip.create(Component.literal("Add 1 offset to west")));
        var up = new IconButton(Icon.ARROW_UP, btn -> changey(1));
        up.setTooltip(Tooltip.create(Component.literal("Add 1 offset up")));
        var down = new IconButton(Icon.ARROW_DOWN, btn -> changey(-1));
        down.setTooltip(Tooltip.create(Component.literal("Add 1 offset down")));
        this.xlabel = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.ylabel = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.zlabel = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        xlabel.setBordered(false);
        ylabel.setBordered(false);
        zlabel.setBordered(false);
        this.widgets.add("skipmissing", skipMissing);
        this.widgets.add("n", north);
        this.widgets.add("s", south);
        this.widgets.add("e", east);
        this.widgets.add("w", west);
        this.widgets.add("u", up);
        this.widgets.add("d", down);
        this.widgets.add("xl", xlabel);
        this.widgets.add("yl", ylabel);
        this.widgets.add("zl", zlabel);
    }

    private void changex(int i) {
        getMenu().xax += i;
        getMenu().syncOffset();
        this.xlabel.setValue(String.valueOf(getMenu().xax));
    }
    private void changey(int i) {
        getMenu().yax += i;
        getMenu().syncOffset();
        this.ylabel.setValue(String.valueOf(getMenu().yax));
    }
    private void changez(int i) {
        getMenu().zax += i;
        getMenu().syncOffset();
        this.zlabel.setValue(String.valueOf(getMenu().zax));
    }

    @Override
    public void updateBeforeRender(){
        super.updateBeforeRender();
        this.setTextContent("missing", !Objects.equals(getMenu().missingItem, "0 Air") ?
                Component.literal(String.format("Missing: %s", getMenu().missingItem))
                : Component.literal("Missing: nothing")
        );
        if (!this.initialized){
            xlabel.setValue(String.valueOf(getMenu().xax));
            ylabel.setValue(String.valueOf(getMenu().yax));
            zlabel.setValue(String.valueOf(getMenu().zax));
            initialized = true;
        }
    }
}
