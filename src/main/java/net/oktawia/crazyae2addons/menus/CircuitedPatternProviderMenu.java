package net.oktawia.crazyae2addons.menus;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.logic.Circuited.CircuitedPatternProviderLogic;
import net.oktawia.crazyae2addons.logic.Circuited.CircuitedPatternProviderLogicHost;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;

public class CircuitedPatternProviderMenu extends AEBaseMenu {

    protected final CircuitedPatternProviderLogic logic;

    @GuiSync(36)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(46)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(56)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(66)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(76)
    public GenericStack unlockStack = null;

    public CircuitedPatternProviderMenu(int id, Inventory playerInventory, CircuitedPatternProviderLogicHost host) {
        super(Menus.CIRCUITED_PATTERN_PROVIDER_MENU, id, playerInventory, host);
        this.createPlayerInventorySlots(playerInventory);
        this.logic = host.getLogic();
        var patternInv = logic.getPatternInv();
        for (int x = 0; x < patternInv.size(); x++) {
            this.addSlot(new AppEngFilteredSlot(patternInv, x, AEItems.PROCESSING_PATTERN.stack()), SlotSemantics.ENCODED_PATTERN);
        }

        var returnInv = logic.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; i++) {
            if (i < returnInv.size()) {
                this.addSlot(new AppEngSlot(returnInv, i), SlotSemantics.STORAGE);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            blockingMode = logic.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInAccessTerminal = logic.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL);
            lockCraftingMode = logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
            craftingLockedReason = logic.getCraftingLockedReason();
            unlockStack = logic.getUnlockStack();
        }
        super.broadcastChanges();
    }

    public YesNo getBlockingMode() {
        return blockingMode;
    }

    public LockCraftingMode getLockCraftingMode() {
        return lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason() {
        return craftingLockedReason;
    }

    public GenericStack getUnlockStack() {
        return unlockStack;
    }

    public YesNo getShowInAccessTerminal() {
        return showInAccessTerminal;
    }
}
