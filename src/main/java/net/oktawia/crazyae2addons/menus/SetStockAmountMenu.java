package net.oktawia.crazyae2addons.menus;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.primitives.Ints;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.logic.Signalling.SignallingInterfaceLogicHost;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SetStockAmountMenu extends AEBaseMenu implements ISubMenu {

    public static final String ACTION_SET_STOCK_AMOUNT = "setStockAmount";

    private final Slot stockedItem;

    private AEKey whatToStock;

    @GuiSync(1)
    private int initialAmount = -1;

    @GuiSync(2)
    private int maxAmount = -1;

    private int slot;

    private final SignallingInterfaceLogicHost host;

    public SetStockAmountMenu(int id, Inventory ip, SignallingInterfaceLogicHost host) {
        super(Menus.SET_STOCK_AMOUNT_MENU, id, ip, host);
        registerClientAction(ACTION_SET_STOCK_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        this.stockedItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.addSlot(this.stockedItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public SignallingInterfaceLogicHost getHost() {
        return host;
    }

    public static void open(ServerPlayer player, MenuLocator locator,
                            int slot,
                            AEKey whatToStock, int initialAmount) {
        MenuOpener.open(Menus.SET_STOCK_AMOUNT_MENU, player, locator);

        if (player.containerMenu instanceof SetStockAmountMenu cca) {
            cca.setWhatToStock(slot, whatToStock, initialAmount);
            cca.broadcastChanges();
        }
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    private void setWhatToStock(int slot, AEKey whatToStock, int initialAmount) {
        this.slot = slot;
        this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
        this.initialAmount = initialAmount;
        this.maxAmount = Ints.saturatedCast(host.getConfig().getMaxAmount(whatToStock));
        this.stockedItem.set(whatToStock.wrapForDisplayOrFilter());
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void confirm(int amount) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_STOCK_AMOUNT, amount);
            return;
        }

        var config = host.getConfig();

        if (!Objects.equals(config.getKey(this.slot), whatToStock)) {
            host.returnToMainMenu(getPlayer(), this);
            return;
        }

        amount = (int) Math.min(amount, config.getMaxAmount(whatToStock));

        if (amount <= 0) {
            config.setStack(slot, null);
        } else {
            config.setStack(slot, new GenericStack(whatToStock, amount));
        }
        host.returnToMainMenu(getPlayer(), this);
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    @Nullable
    public AEKey getWhatToStock() {
        var stack = GenericStack.fromItemStack(stockedItem.getItem());
        return stack != null ? stack.what() : null;
    }
}
