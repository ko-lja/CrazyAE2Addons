package net.oktawia.crazyae2addons.menus;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.CrazyPatternProviderBE;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.UpdatePatternsPacket;

import java.util.ArrayList;
import java.util.List;

public class CrazyPatternProviderMenu extends PatternProviderMenu {

    private static String SYNC = "patternSync";
    private final PatternProviderLogicHost host;
    private final Player player;
    @GuiSync(38)
    public Integer slotNum;

    public CrazyPatternProviderMenu(int id, Inventory ip, PatternProviderLogicHost host) {
        super(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), id, ip, host);
        this.host = host;
        this.player = ip.player;
        if (host.getBlockEntity() instanceof CrazyPatternProviderBE crazyBE){
            this.slotNum = crazyBE.getAdded() * 9 + 8 * 9;
        } else {
            this.slotNum = 8 * 9;
        }
        registerClientAction(SYNC, this::requestUpdate);
    }

    public void requestUpdate() {
        if (isClientSide()){
            sendClientAction(SYNC);
        } else {
            var inventory = this.host.getLogic().getPatternInv();
            List<ItemStack> visibleStacks = new ArrayList<>();

            for (int i = 0; i < this.slotNum; i++) {
                visibleStacks.add(inventory.getStackInSlot(i));
            }

            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new UpdatePatternsPacket(visibleStacks)
            );
        }
    }

    @Override
    public void onSlotChange(Slot s){
        if (isClientSide()){
            this.sendAllDataToRemote();
        }
    }
}