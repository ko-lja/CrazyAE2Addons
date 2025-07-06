package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.AutoBuilderBE;
import net.oktawia.crazyae2addons.misc.UnifiedAutoBuilderSlot;

public class AutoBuilderMenu extends UpgradeableMenu<AutoBuilderBE> {

    @GuiSync(238)
    public int xax;
    @GuiSync(237)
    public int yax;
    @GuiSync(236)
    public int zax;

    @GuiSync(219)
    public String missingItem;
    private String MISSING = "actionUpdateMissing";
    private String OFFSET = "actionUpdateOffset";
    @GuiSync(932)
    public boolean skipEmpty;

    public AutoBuilderMenu(int id, Inventory playerInventory, AutoBuilderBE host) {
        super(CrazyMenuRegistrar.AUTO_BUILDER_MENU.get(), id, playerInventory, host);
        this.missingItem = String.format("%s %s", host.missingItems.getCount(), host.missingItems.getItem().getDescription().getString());
        this.addSlot(new UnifiedAutoBuilderSlot(host.inventory, 0), SlotSemantics.ENCODED_PATTERN);
        this.xax = host.offset.getX();
        this.yax = host.offset.getY();
        this.zax = host.offset.getZ();
        this.skipEmpty = host.skipEmpty;
        this.registerClientAction(MISSING, Boolean.class, this::updateMissing);
        this.registerClientAction(OFFSET, String.class, this::syncOffset);
    }

    public void updateMissing(boolean selected) {
        getHost().skipEmpty = selected;
        this.skipEmpty = selected;
        if (isClientSide()){
            sendClientAction(MISSING, selected);
        }
    }

    public void syncOffset() {
        syncOffset("%s|%s|%s".formatted(xax, yax, zax));
    }

    public void syncOffset(String offset) {
        xax = Integer.parseInt(offset.split("\\|")[0]);
        yax = Integer.parseInt(offset.split("\\|")[1]);
        zax = Integer.parseInt(offset.split("\\|")[2]);
        getHost().offset = new BlockPos(xax, yax, zax);
        getHost().setGhostRenderPos(getHost().getBlockPos().offset(getHost().offset));
        if (isClientSide()){
            sendClientAction(OFFSET, offset);
        }
    }
}
