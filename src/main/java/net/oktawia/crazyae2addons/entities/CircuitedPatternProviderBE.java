package net.oktawia.crazyae2addons.entities;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Menus;

public class CircuitedPatternProviderBE extends PatternProviderBlockEntity {

    public CircuitedPatternProviderBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void setPriority(int newValue) {
        super.setPriority(newValue);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.CIRCUITED_PATTERN_PROVIDER_MENU, player, locator);
    }

    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this, 36);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(Menus.CIRCUITED_PATTERN_PROVIDER_MENU, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(Blocks.CIRCUITED_PATTERN_PROVIDER_BLOCK.asItem());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return Blocks.CIRCUITED_PATTERN_PROVIDER_BLOCK.stack();
    }

}