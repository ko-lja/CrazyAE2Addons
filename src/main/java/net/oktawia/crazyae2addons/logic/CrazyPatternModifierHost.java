package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.menus.CrazyPatternModifierMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class CrazyPatternModifierHost extends ItemMenuHost implements InternalInventoryHost {
    public Deque<Integer> lastFiveCirc = new ArrayDeque<>();
    public final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private CrazyPatternModifierMenu menu;

    public CrazyPatternModifierHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);

        CompoundTag itemTag = this.getItemStack().getTag();
        if (itemTag != null) {
            this.inv.readFromNBT(itemTag, "inv");
            for (int i = 0; i < itemTag.getInt("circHistorySize"); i++) {
                lastFiveCirc.add(itemTag.getInt("circHistory_" + i));
            }
        }
    }

    public Deque<Integer> getLastFiveCirc() {
        return lastFiveCirc;
    }

    public void setLastFiveCirc(Deque<Integer> circ){
        this.lastFiveCirc = circ;
    }

    @Override
    public void saveChanges() {
        CompoundTag itemTag = this.getItemStack().getOrCreateTag();
        this.inv.writeToNBT(itemTag, "inv");
        itemTag.putInt("circHistorySize", lastFiveCirc.size());
        int i = 0;
        for (int val : lastFiveCirc) {
            itemTag.putInt("circHistory_" + i, val);
            i++;
        }
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        CompoundTag itemTag = this.getItemStack().getOrCreateTag();
        if (inv == this.inv) {
            this.inv.writeToNBT(itemTag, "inv");
            if (this.getMenu() != null){
                this.getMenu().ping();
            }
        }
    }

    public void setMenu(CrazyPatternModifierMenu menu) {
        this.menu = menu;
    }

    public CrazyPatternModifierMenu getMenu(){
        return this.menu;
    }
}
