package net.oktawia.crazyae2addons.menus;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.entities.PatternModifierBE;

public class PatternModifierMenu extends UpgradeableMenu<PatternModifierBE> {

    public static String CHANGE_IGNORE_NBT = "changeIgnoreNBT";
    public static String CHANGE_CIRCUIT = "changeCircuit";

    @GuiSync(892)
    public String textNBT = "No Item";
    @GuiSync(92)
    public String textCirc = "Circuit setting: 0";

    public PatternModifierMenu(int id, Inventory ip, PatternModifierBE host) {
        super(Menus.PATTERN_MODIFIER_MENU, id, ip, host);
        this.getHost().setMenu(this);
        this.addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.STORAGE);
        registerClientAction(CHANGE_IGNORE_NBT, this::changeIgnoreNBT);
        registerClientAction(CHANGE_CIRCUIT, Integer.class, this::changeCircuit);
    }

    public void changeIgnoreNBT(){
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            return;
        }
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        if (isValidItem(AEItemKey.of(item))){
            CompoundTag currentTag = item.getOrCreateTag();
            boolean tag;
            if (currentTag.contains("ignorenbt")){
                tag = !currentTag.getBoolean("ignorenbt");
            } else {
                tag = true;
            }
            currentTag.putBoolean("ignorenbt", tag);
            item.setTag(currentTag);
        }
        updateText(item);
        if (isClientSide()){
            sendClientAction(CHANGE_IGNORE_NBT);
        }
    }


    public void changeCircuit(int val){
        if (this.getSlots(SlotSemantics.STORAGE).get(0).getItem().isEmpty()){
            return;
        }
        ItemStack item = this.getSlots(SlotSemantics.STORAGE).get(0).getItem();
        if (isValidItem(AEItemKey.of(item))){
            CompoundTag currentTag = item.getOrCreateTag();
            currentTag.putInt("circuit", val);
            item.setTag(currentTag);
        }
        updateText(item);
        if (isClientSide()){
            sendClientAction(CHANGE_CIRCUIT, val);
        }
    }


    public void updateText(ItemStack stack){
        if (stack.isEmpty()){
            this.textNBT = "No Item";
            return;
        }
        AEItemKey item = AEItemKey.of(stack);
        if (item != null && isValidItem(item)){
            CompoundTag currentTag = item.getTag();
            boolean nbtIgnoreValue = currentTag.getBoolean("ignorenbt");
            if (nbtIgnoreValue){
                this.textNBT = "Setting: Ignore NBT";
            } else {
                this.textNBT = "Setting: DO NOT Ignore NBT";
            }
            if (currentTag.contains("circuit")){
                int circ = currentTag.getInt("circuit");
                this.textCirc = "Circuit setting: " + circ;
            } else {
                this.textCirc = "Circuit setting: 0";
            }
        } else {
            this.textNBT = "Invalid Item";
        }
    }

    public boolean isValidItem(AEItemKey item) {
        return item.fuzzyEquals(AEItemKey.of(AEItems.PROCESSING_PATTERN.stack()), FuzzyMode.IGNORE_ALL);
    }
}
