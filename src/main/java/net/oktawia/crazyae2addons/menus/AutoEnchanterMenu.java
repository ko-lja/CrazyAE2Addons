package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.AutoEnchanterBE;

public class AutoEnchanterMenu extends UpgradeableMenu<AutoEnchanterBE> {
    public String fluidCapacity = "32000";

    @GuiSync(173)
    public String fluidAmount = String.valueOf(getHost().fluidInv.getFluidAmount());

    public String ACTION_SYNC_LEVEL = "actionSyncLevel";

    @GuiSync(894)
    public int selectedLevel = getHost().selectedLevel;

    public AutoEnchanterMenu(int id, Inventory ip, AutoEnchanterBE host) {
        super(Menus.AUTO_ENCHANTER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        registerClientAction(ACTION_SYNC_LEVEL, Integer.class, this::syncLevel);
        this.addSlot(new RestrictedSlot(getHost().inputInv.toContainer(), 0, 0, 0, Items.BOOK.getDefaultInstance()), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new RestrictedSlot(getHost().inputInv.toContainer(), 1, 0, 0, Items.LAPIS_LAZULI.getDefaultInstance()), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new ExtractSlot(getHost().outputInv.toContainer(), 0, 0, 0), SlotSemantics.MACHINE_OUTPUT);

    }

    public void syncLevel(int level) {
        getHost().selectedLevel = level;
        this.selectedLevel = level;
        CompoundTag tag = getHost().getPersistentData();
        tag.putInt("level", level);
        getHost().markForUpdate();
        if (isClientSide()){
            sendClientAction(ACTION_SYNC_LEVEL, level);
        }
    }
}
