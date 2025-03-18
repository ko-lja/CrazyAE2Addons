package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import com.mojang.logging.LogUtils;
import net.oktawia.crazyae2addons.interfaces.IIgnoreNBT;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ListCraftingInventory.class)
public class MixinListCraftingInventory implements IIgnoreNBT {

    @Final
    @Shadow
    public KeyCounter list;

    @Final
    @Shadow
    private ListCraftingInventory.ChangeListener listener;

    @Unique
    public boolean ignoreNBT = false;

    @Unique
    @Override
    public void setIgnoreNBT(boolean mode){
        this.ignoreNBT = mode;
    }

    @Override
    public boolean getIgnoreNBT() {
        return this.ignoreNBT;
    }

    /**
     * @author oktawia
     * @reason add ignore nbt option
     */
    @Overwrite(remap = false)
    public long extract(AEKey what, long amount, Actionable mode) {
        long available;
        if (ignoreNBT){
            available = list.findFuzzy(what, FuzzyMode.IGNORE_ALL).size();
        } else {
            available = list.get(what);
        }
        var extracted = Math.min(available, amount);
        if (mode == Actionable.MODULATE) {
            if (available > extracted) {
                list.remove(what, extracted);
            } else {
                list.remove(what);
            }
            listener.onChange(what);
        }
        return extracted;
    }

}
