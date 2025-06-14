package net.oktawia.crazyae2addons.screens;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.definitions.AEItems;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.entities.PenroseControllerBE;
import net.oktawia.crazyae2addons.menus.PenroseControllerMenu;
import net.oktawia.crazyae2addons.misc.IconButton;

public class PenroseControllerScreen<C extends PenroseControllerMenu> extends AEBaseScreen<C> {
    public PenroseControllerScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        var extractBtn = new IconButton(Icon.ARROW_UP, (btn) -> getMenu().extractFromCell());
        var insertBtn = new IconButton(Icon.ARROW_DOWN, (btn) -> getMenu().insertToCell());
        extractBtn.setTooltip(Tooltip.create(Component.literal("Extract singularities")));
        insertBtn.setTooltip(Tooltip.create(Component.literal("Insert singularities")));
        this.widgets.add("add", extractBtn);
        this.widgets.add("remove", insertBtn);
    }

    @Override
    public void updateBeforeRender() {
        super.updateBeforeRender();
        setTextContent("generation", Component.literal("Power Generation"));
        var disk = StorageCells.getCellInventory(getMenu().diskSlot.getItem(), null);
        long generated;
        if (disk == null){
            generated = 0;
        } else {
            generated = PenroseControllerBE.energyGenerated((int) disk.getAvailableStacks().get(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get())));
        }
        if (AEItems.MATTER_BALL.isSameAs(getMenu().configSlot.getItem())){
            generated *= 8;
        } else if (AEItems.SINGULARITY.isSameAs(getMenu().configSlot.getItem())){
            generated *= 64;
        }
        setTextContent("amount", Component.literal(String.format("%s FE/t", Utils.shortenNumber(generated))));
    }
}
