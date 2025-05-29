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

public class CrazyPatternModifierMenu extends AEBaseMenu {

    public static String CHANGE_IGNORE_NBT = "changeIgnoreNBT";
    public static String CHANGE_CIRCUIT = "changeCircuit";

    @GuiSync(892)
    public String textNBT = "";
    @GuiSync(92)
    public String textCirc = "";

    public CrazyPatternModifierMenu(int id, Inventory ip, CrazyPatternModifierHost host) {
        super(CrazyMenuRegistrar.CRAZY_PATTERN_MODIFIER_MENU.get(), id, ip, host);
        this.createPlayerInventorySlots(ip);
        host.setMenu(this);
        this.addSlot(new AppEngFilteredSlot(host.inv, 0, AEItems.PROCESSING_PATTERN.asItem()), SlotSemantics.STORAGE);
        registerClientAction(CHANGE_IGNORE_NBT, this::changeNBT);
        registerClientAction(CHANGE_CIRCUIT, Integer.class, this::changeCircuit);
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

    public void changeCircuit(int val){
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            return;
        }
        if (isClientSide()){
            sendClientAction(CHANGE_CIRCUIT, val);
        } else {
            ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
            CompoundTag tag = item.getOrCreateTag();
            if (tag.contains("circuit")) {
                if (val == -1) {
                    tag.remove("circuit");
                    this.textCirc = "No circuit selected";
                }
            }
            if (val != -1){
                tag.putInt("circuit", val);
                this.textCirc = "Selected circuit " + val;
            }
            item.setTag(tag);
        }
    }

    public void ping() {
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            this.textCirc = "";
            this.textNBT = "";
            return;
        }
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        CompoundTag tag = item.getOrCreateTag();
        if (tag.contains("circuit")) {
            this.textCirc = "Selected circuit " + tag.getInt("circuit");
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
