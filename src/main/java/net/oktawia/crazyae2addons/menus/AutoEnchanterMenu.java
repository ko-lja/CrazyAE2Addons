package net.oktawia.crazyae2addons.menus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.AutoEnchanterBE;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;
import net.oktawia.crazyae2addons.misc.ExtractSlot;


public class AutoEnchanterMenu extends UpgradeableMenu<AutoEnchanterBE> {
    public int xpCap = 1600;

    @GuiSync(173)
    public int holdedXp = getHost().holdedXp;

    public String ACTION_SYNC_LEVEL = "actionSyncLevel";

    @GuiSync(894)
    public int selectedLevel = getHost().selectedLevel;

    public AutoEnchanterMenu(int id, Inventory ip, AutoEnchanterBE host) {
        super(Menus.AUTO_ENCHANTER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        registerClientAction(ACTION_SYNC_LEVEL, Integer.class, this::syncLevel);
        this.addSlot(new AppEngFilteredSlot(getHost().inputExposedBook, 0, Items.BOOK.getDefaultInstance()), SlotSemantics.STORAGE);
        this.addSlot(new AppEngFilteredSlot(getHost().inputExposedLapis, 0, Items.LAPIS_LAZULI.getDefaultInstance()), SlotSemantics.STORAGE);
        this.addSlot(new AppEngFilteredSlot(getHost().inputExposedXpShards, 0, net.oktawia.crazyae2addons.defs.Items.XP_SHARD_ITEM.stack()), SlotSemantics.STORAGE);
        this.addSlot(new ExtractSlot(getHost().outputExposed.toContainer(), 0, 0, 0), SlotSemantics.MACHINE_OUTPUT);
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
