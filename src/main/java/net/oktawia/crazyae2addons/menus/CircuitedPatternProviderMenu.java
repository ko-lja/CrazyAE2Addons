package net.oktawia.crazyae2addons.menus;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;

public class CircuitedPatternProviderMenu extends PatternProviderMenu {
    public CircuitedPatternProviderMenu(int id, Inventory playerInventory, PatternProviderLogicHost host) {
        super(CrazyMenuRegistrar.CIRCUITED_PATTERN_PROVIDER_MENU.get(), id, playerInventory, host);
    }
}
