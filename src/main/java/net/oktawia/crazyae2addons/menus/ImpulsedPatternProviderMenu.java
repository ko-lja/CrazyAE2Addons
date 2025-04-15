package net.oktawia.crazyae2addons.menus;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.core.definitions.AEItems;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.logic.Impulsed.ImpulsedPatternProviderLogic;
import net.oktawia.crazyae2addons.logic.Impulsed.ImpulsedPatternProviderLogicHost;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;

public class ImpulsedPatternProviderMenu extends AEBaseMenu {

    protected final ImpulsedPatternProviderLogic logic;

    @GuiSync(56)
    public RedstoneMode blockingModeButton = RedstoneMode.SIGNAL_PULSE;
    @GuiSync(66)
    public LockCraftingMode lockCraftingModeButton = LockCraftingMode.LOCK_UNTIL_RESULT;

    public ImpulsedPatternProviderMenu(int id, Inventory playerInventory, ImpulsedPatternProviderLogicHost host) {
        super(Menus.IMPULSED_PATTERN_PROVIDER_MENU, id, playerInventory, host);
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
            blockingModeButton = logic.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
            lockCraftingModeButton = logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
        }
        super.broadcastChanges();
    }

    public RedstoneMode getBlockingMode() {
        return RedstoneMode.SIGNAL_PULSE;
    }

    public LockCraftingMode getLockCraftingMode() {
        return LockCraftingMode.LOCK_UNTIL_RESULT;
    }
}
