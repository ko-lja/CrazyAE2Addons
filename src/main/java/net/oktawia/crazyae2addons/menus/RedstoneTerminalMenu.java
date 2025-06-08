package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.misc.BlockPosAdapter;
import net.oktawia.crazyae2addons.parts.ChunkyFluidP2PTunnelPart;
import net.oktawia.crazyae2addons.parts.RedstoneTerminalPart;

import java.util.List;


public class RedstoneTerminalMenu extends AEBaseMenu {
    public record EmitterInfo(BlockPos pos, String name, boolean active) { }
    public String TOGGLE = "syncToggle";
    public RedstoneTerminalPart host;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockPos.class, new BlockPosAdapter())
            .create();

    @GuiSync(39)
    public String emitters;

    public RedstoneTerminalMenu(int id, Inventory ip, RedstoneTerminalPart host) {
        super(CrazyMenuRegistrar.REDSTONE_TERMINAL_MENU.get(), id, ip, host);
        this.host = host;
        if (!isClientSide()){
            this.emitters = GSON.toJson(host.getEmitters());
        }
        registerClientAction(TOGGLE, String.class, this::toggle);
    }

    public void toggle(String name) {
        if (isClientSide()){
            sendClientAction(TOGGLE, name);
        } else {
            host.toggle(name);
            this.emitters = GSON.toJson(host.getEmitters());
        }
    }
}
