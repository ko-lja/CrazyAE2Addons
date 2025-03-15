package net.oktawia.crazyae2addons.menus;

import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.CraftingCancelerBE;

public class CraftingCancelerMenu extends UpgradeableMenu<CraftingCancelerBE> {
    private final ContainerLevelAccess access;
    private final BlockEntity blockEntity;

    public Boolean en = false;
    public Integer dur = 0;
    private static final String ACTION_SEND_STATE = "ActionSendState";
    private static final String ACTION_SEND_DURATION = "ActionSendDuration";

    public CraftingCancelerMenu(int id, Inventory playerInventory, FriendlyByteBuf additional){
        this(id, playerInventory, (CraftingCancelerBE) playerInventory.player.level().getBlockEntity(additional.readBlockPos()));
    }

    public CraftingCancelerMenu(int id, Inventory ip, CraftingCancelerBE host) {
        super(Menus.CRAFTING_CANCELER_MENU, id, ip, host);
        this.access = ContainerLevelAccess.create(ip.player.level(), host.getBlockPos());
        this.blockEntity = host;

        registerClientAction(ACTION_SEND_STATE, Boolean.class, this::sendState);
        registerClientAction(ACTION_SEND_DURATION, Integer.class, this::sendDuration);
        setGuiState(this.getHost().getEnabled(), this.getHost().getDuration());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((world, pos) -> world.getBlockEntity(pos) == this.blockEntity, true);
    }
    public void sendState(boolean state){
        if (isClientSide()){
            sendClientAction(ACTION_SEND_STATE, state);
        }
        else{
            this.getHost().setEnabled(state);
            setGuiState(state, this.getHost().getDuration());
        }
    }

    public void sendDuration(int duration){
        if (isClientSide()){
            sendClientAction(ACTION_SEND_DURATION, duration);
        }
        else{
            this.getHost().setDuration(duration);
            setGuiState(this.getHost().getEnabled(), duration);
        }
    }

    public void setGuiState(boolean en, int dur){
        this.en = en;
        this.dur = dur;
        broadcastChanges();
    }
}
