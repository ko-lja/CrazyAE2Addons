package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.menus.CrazyEmitterMultiplierMenu;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuilderPatternHost extends ItemMenuHost {

    private String program = "";
    private boolean code;
    private int delay = 0;

    public BuilderPatternHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
        var tag = getItemStack().getOrCreateTag();
        if (tag.contains("program")){
            this.program = tag.getString("program");
        }
        if (tag.contains("code")){
            this.code = tag.getBoolean("code");
        }
        if (tag.contains("delay")){
            this.delay = tag.getInt("delay");
        }
    }

    public String getProgram() {
        return this.program;
    }
    public int getDelay() {
        return this.delay;
    }
    public void setProgram(String program) {
        this.program = program;
        this.getItemStack().getOrCreateTag().putString("program", program);
    }
    public void setDelay(int delay) {
        this.delay = delay;
        this.getItemStack().getOrCreateTag().putInt("delay", delay);
    }

    public void validate() {
        if (!program.isEmpty()){
            ProgramExpander.Result result = ProgramExpander.expand(program);
            this.code = result.success;
            this.getItemStack().getOrCreateTag().putBoolean("code", this.code);
        }
    }
}
