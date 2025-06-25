package net.oktawia.crazyae2addons.entities;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.mixins.PatternProviderBlockEntityAccessor;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.SyncBlockClientPacket;

public class CrazyPatternProviderBE extends PatternProviderBlockEntity {
    private int added = 0;

    public CrazyPatternProviderBE(BlockPos pos, BlockState blockState) {
        this(pos, blockState, 9*8);
    }

    public CrazyPatternProviderBE(BlockPos pos, BlockState blockState, Integer patternSize){
        super(CrazyBlockEntityRegistrar.CRAZY_PATTERN_PROVIDER_BE.get(), pos, blockState);
        this.getMainNode()
                .setVisualRepresentation(CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get().asItem());
        ((PatternProviderBlockEntityAccessor)this).setLogic(new PatternProviderLogic(getMainNode(), this, patternSize));
    }

    public CrazyPatternProviderBE refreshLogic(int added) {
        var oldInv = (AppEngInternalInventory) this.logic.getPatternInv();
        CompoundTag tag = new CompoundTag();
        oldInv.writeToNBT(tag, "patterns");

        BlockPos pos = this.getBlockPos();
        level.removeBlockEntity(pos);

        level.setBlockEntity(new CrazyPatternProviderBE(pos, level.getBlockState(pos), 8 * 9 + 9 * added));

        var newBE = (CrazyPatternProviderBE) level.getBlockEntity(pos);
        if (newBE == null) return this;

        var newLogic = newBE.getLogic();
        var newInv = (AppEngInternalInventory) newLogic.getPatternInv();
        newInv.readFromNBT(tag, "patterns");

        newBE.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }

        return newBE;
    }

    public void incrementAdded() {
        added++;
    }

    public int getAdded() {
        return added;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("added", added);
    }

    @Override
    public void onReady(){
        super.onReady();
        if (this.getLogic().getPatternInv().size() != 8 * 9 + 9 * added){
            var be = refreshLogic(added);
            be.added = added;
        }
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        added = data.getInt("added");
    }

    public void setAdded(int amt){
        if (amt != this.added){
            this.added = amt;
            var be = this.refreshLogic(added);
            be.added = added;
        }
    }

    @Override
    public PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this, 9 * 8 + (this.getAdded() * 9));
    }

    @Override
    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
    }
}
