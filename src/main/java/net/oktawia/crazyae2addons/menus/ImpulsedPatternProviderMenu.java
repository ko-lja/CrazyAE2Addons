package net.oktawia.crazyae2addons.menus;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;

public class ImpulsedPatternProviderMenu extends PatternProviderMenu {
    public ImpulsedPatternProviderMenu(int id, Inventory playerInventory, PatternProviderLogicHost host) {
        super(CrazyMenuRegistrar.IMPULSED_PATTERN_PROVIDER_MENU.get(), id, playerInventory, host);
    }
}
