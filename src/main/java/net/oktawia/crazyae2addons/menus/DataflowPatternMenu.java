package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.logic.BuilderPatternHost;
import net.oktawia.crazyae2addons.logic.DataflowPatternHost;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.SendLongStringToClientPacket;
import net.oktawia.crazyae2addons.network.SendLongStringToServerPacket;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataflowPatternMenu extends AEBaseMenu {
    public DataflowPatternMenu(int id, Inventory playerInventory, DataflowPatternHost host) {
        super(CrazyMenuRegistrar.DATAFLOW_PATTERN_MENU.get(), id, playerInventory, host);
        this.createPlayerInventorySlots(playerInventory);
    }
}
