package net.oktawia.crazyae2addons.screens;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.definitions.AEItems;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.entities.PenroseControllerBE;
import net.oktawia.crazyae2addons.menus.PenroseControllerMenu;
import net.oktawia.crazyae2addons.misc.IconButton;

import java.util.List;

public class PenroseControllerScreen<C extends PenroseControllerMenu> extends AEBaseScreen<C> {
    public ToggleButton powerMode;
    public PenroseControllerScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        var extractBtn = new IconButton(Icon.ARROW_UP, (btn) -> getMenu().extractFromCell());
        var insertBtn = new IconButton(Icon.ARROW_DOWN, (btn) -> getMenu().insertToCell());
        this.powerMode = new ToggleButton(Icon.POWER_UNIT_AE, Icon.POWER_UNIT_RF, this::changePowerMode);
        extractBtn.setTooltip(Tooltip.create(Component.literal("Extract singularities")));
        insertBtn.setTooltip(Tooltip.create(Component.literal("Insert singularities")));
        this.powerMode.setTooltipOn(List.of(Component.literal("Store power"), Component.literal("as AE in the network power")));
        this.powerMode.setTooltipOff(List.of(Component.literal("Store power"), Component.literal("as FE in the multiblock")));
        this.widgets.add("add", extractBtn);
        this.widgets.add("remove", insertBtn);
        this.widgets.add("energy", powerMode);
        var prevBtn = new IconButton(Icon.ENTER, btn -> getMenu().changePreview(!getMenu().preview));
        prevBtn.setTooltip(Tooltip.create(Component.literal("Enable/Disable preview")));
        var tierBtn = new IconButton(Icon.ENTER, btn -> getMenu().changePrevTier((getMenu().previewTier + 1) % 4));
        tierBtn.setTooltip(Tooltip.create(Component.literal("Change preview tier")));
        this.widgets.add("prevbtn", prevBtn);
        this.widgets.add("tierbtn", tierBtn);
    }

    private void changePowerMode(boolean b) {
        this.powerMode.setState(!b);
        this.getMenu().changeEnergyMode(b);
    }

    @Override
    public void updateBeforeRender() {
        super.updateBeforeRender();
        setTextContent("generation", Component.literal("Power Generation"));
        var disk0 = StorageCells.getCellInventory(getMenu().diskSlot0.getItem(), null);
        var disk1 = StorageCells.getCellInventory(getMenu().diskSlot1.getItem(), null);
        var disk2 = StorageCells.getCellInventory(getMenu().diskSlot2.getItem(), null);
        var disk3 = StorageCells.getCellInventory(getMenu().diskSlot3.getItem(), null);
        long generated;
        var tier = getMenu().tier;
        int count = 0;
        if (disk0 != null && tier >= 0) {
            count += (int) disk0.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
        } if (disk1 != null && tier >= 1) {
            count += (int) disk1.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
        } if (disk2 != null && tier >= 2) {
            count += (int) disk2.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
        } if (disk3 != null && tier >= 3) {
            count += (int) disk3.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
        }

        generated = PenroseControllerBE.energyGenerated(count, getMenu().tier);

        if (AEItems.MATTER_BALL.isSameAs(getMenu().configSlot.getItem())){
            generated *= 8;
        } else if (AEItems.SINGULARITY.isSameAs(getMenu().configSlot.getItem())){
            generated *= 64;
        }

        this.powerMode.setState(getMenu().powerMode);
        setTextContent("amount", Component.literal(String.format("%s FE/t", Utils.shortenNumber(generated))));
        setTextContent("tier", Component.literal("Tier: " + getMenu().tier));
        setTextContent("prev", Component.literal("Preview: " + getMenu().preview + ", preview tier: " + getMenu().previewTier));
    }
}
