package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import net.oktawia.crazyae2addons.interfaces.IIgnoreNBT;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ListCraftingInventory.class)
public class MixinListCraftingInventory implements IIgnoreNBT {

    @Shadow private KeyCounter list;
    @Unique private boolean ignoreNBT = false;

    @Redirect(
            method = "extract(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/stacks/KeyCounter;get(Lappeng/api/stacks/AEKey;)J"
            )
    )
    private long redirectGet(KeyCounter instance, AEKey key) {
        if (getIgnoreNBT()) {
            return instance.findFuzzy(key, FuzzyMode.IGNORE_ALL).size();
        } else {
            return instance.get(key);
        }
    }

    @Unique
    @Override
    public void setIgnoreNBT(boolean mode) {
        this.ignoreNBT = mode;
    }

    @Unique
    @Override
    public boolean getIgnoreNBT() {
        return this.ignoreNBT;
    }
}
