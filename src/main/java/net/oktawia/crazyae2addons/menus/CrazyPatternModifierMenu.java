package net.oktawia.crazyae2addons.menus;

import appeng.core.definitions.AEItems;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.logic.CrazyPatternModifierHost;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;
import net.oktawia.crazyae2addons.misc.AppEngManyFilteredSlot;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class CrazyPatternModifierMenu extends AEBaseMenu {

    public static String CHANGE_IGNORE_NBT = "changeIgnoreNBT";
    public static String CHANGE_CIRCUIT = "changeCircuit";
    public static String CHANGE_CIRCUIT2 = "changeCircuit2";

    @GuiSync(892)
    public String textNBT = "";
    @GuiSync(92)
    public String textCirc = "";
    @GuiSync(501)
    public int history1 = -1;
    @GuiSync(502)
    public int history2 = -1;
    @GuiSync(503)
    public int history3 = -1;
    @GuiSync(504)
    public int history4 = -1;
    @GuiSync(505)
    public int history5 = -1;

    private CrazyPatternModifierHost host;

    public CrazyPatternModifierMenu(int id, Inventory ip, CrazyPatternModifierHost host) {
        super(CrazyMenuRegistrar.CRAZY_PATTERN_MODIFIER_MENU.get(), id, ip, host);
        this.createPlayerInventorySlots(ip);
        host.setMenu(this);
        this.host = host;
        Integer[] arr = host.getLastFiveCirc().toArray(new Integer[0]);
        history1 = arr.length > 0 ? arr[0] : -1;
        history2 = arr.length > 1 ? arr[1] : -1;
        history3 = arr.length > 2 ? arr[2] : -1;
        history4 = arr.length > 3 ? arr[3] : -1;
        history5 = arr.length > 4 ? arr[4] : -1;
        this.addSlot(new AppEngManyFilteredSlot(host.inv, 0, List.of(AEItems.PROCESSING_PATTERN.stack(), AEItems.CRAFTING_PATTERN.stack())), SlotSemantics.STORAGE);
        registerClientAction(CHANGE_IGNORE_NBT, this::changeNBT);
        registerClientAction(CHANGE_CIRCUIT, Integer.class, this::changeCircuit);
        registerClientAction(CHANGE_CIRCUIT2, Integer.class, this::changeCircuit2);
    }

    public void changeNBT(){
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            return;
        }
        if (isClientSide()){
            sendClientAction(CHANGE_IGNORE_NBT);
        } else {
            ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
            CompoundTag tag = item.getOrCreateTag();
            if (tag.contains("ignorenbt")){
                tag.remove("ignorenbt");
                this.textNBT = "Current: Do not ignore NBT";
            } else {
                tag.putBoolean("ignorenbt", true);
                this.textNBT = "Current: ignore NBT";
            }
            item.setTag(tag);
        }
    }

    public void changeCircuit(int val) {
        changeCircuit(val, true);
    }

    public void changeCircuit2(int val) {
        changeCircuit(val, false);
    }

    public void changeCircuit(int val, boolean recordHistory){
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            return;
        }
        if (isClientSide()){
            if (recordHistory){
                sendClientAction(CHANGE_CIRCUIT, val);
            } else {
                sendClientAction(CHANGE_CIRCUIT2, val);
            }
        } else {
            ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
            CompoundTag tag = item.getOrCreateTag();

            if (val == -1) {
                tag.remove("circuit");
                tag.remove("CustomModelData");
                this.textCirc = "No circuit selected";
            } else {
                tag.putInt("circuit", val);
                tag.putInt("CustomModelData", val == 0 ? 33 : val);
                this.textCirc = "Selected circuit " + val;

                if (recordHistory) {
                    Deque<Integer> history = getLastFiveCirc();
                    history.remove(val);
                    history.addFirst(val);
                    while (history.size() > 5) {
                        history.removeLast();
                    }

                    Integer[] arr = history.toArray(new Integer[0]);
                    history1 = arr.length > 0 ? arr[0] : -1;
                    history2 = arr.length > 1 ? arr[1] : -1;
                    history3 = arr.length > 2 ? arr[2] : -1;
                    history4 = arr.length > 3 ? arr[3] : -1;
                    history5 = arr.length > 4 ? arr[4] : -1;
                    host.setLastFiveCirc(history);
                    host.saveChanges();
                }
            }

            item.setTag(tag);
        }
    }

    public Deque<Integer> getLastFiveCirc() {
        return host.getLastFiveCirc();
    }

    public void ping() {
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        CompoundTag tag = item.getOrCreateTag();

        if (tag.contains("circuit")) {
            int val = tag.getInt("circuit");
            this.textCirc = "Selected circuit " + val;

            Deque<Integer> history = getLastFiveCirc();

            if (!history.contains(val)) {
                history.addFirst(val);
                while (history.size() > 5) {
                    history.removeLast();
                }
            } else {
                history.remove(val);
                history.addFirst(val);
            }

            Integer[] arr = history.toArray(new Integer[0]);
            history1 = arr.length > 0 ? arr[0] : -1;
            history2 = arr.length > 1 ? arr[1] : -1;
            history3 = arr.length > 2 ? arr[2] : -1;
            history4 = arr.length > 3 ? arr[3] : -1;
            history5 = arr.length > 4 ? arr[4] : -1;

        } else {
            this.textCirc = "No circuit selected";
        }

        if (tag.contains("ignorenbt")) {
            this.textNBT = "Current: ignore NBT";
        } else {
            this.textNBT = "Current: Do not ignore NBT";
        }
    }
}
