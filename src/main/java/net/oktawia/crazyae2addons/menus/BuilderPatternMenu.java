package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.AutoBuilderBE;
import net.oktawia.crazyae2addons.logic.BuilderPatternHost;

public class BuilderPatternMenu extends AEBaseMenu {
    public static final String SEND_DATA = "SendData";
    public static final String SEND_DELAY = "SendDelay";

    @GuiSync(92)
    public String program;
    public BuilderPatternHost host;
    @GuiSync(93)
    public Integer delay;

    public BuilderPatternMenu(int id, Inventory playerInventory, BuilderPatternHost host) {
        super(CrazyMenuRegistrar.BUILDER_PATTERN_MENU.get(), id, playerInventory, host);
        registerClientAction(SEND_DATA, String.class, this::updateData);
        registerClientAction(SEND_DELAY, Integer.class, this::updateDelay);
        this.host = host;
        this.program = host.getProgram();
        this.delay = host.getDelay();
        this.createPlayerInventorySlots(playerInventory);
    }

    public void updateData(String program) {
        this.program = program;
        this.host.setProgram(program);
        if (isClientSide()){
            sendClientAction(SEND_DATA, program);
        } else {
            this.host.validate();
        }
    }

    public void updateDelay(Integer delay) {
        if (delay < 0) delay = 0;
        this.delay = delay;
        this.host.setDelay(delay);
        if (isClientSide()){
            sendClientAction(SEND_DELAY, delay);
        } else {
            this.host.validate();
        }
    }
}
