package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.oktawia.crazyae2addons.menus.CrazyEmitterMultiplierMenu;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BuilderPatternHost extends ItemMenuHost {

    private String program = "";
    private boolean code;
    private int delay = 0;

    public BuilderPatternHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
        var tag = getItemStack().getOrCreateTag();
        if (tag.contains("code")){
            this.code = tag.getBoolean("code");
        }
        if (tag.contains("delay")){
            this.delay = tag.getInt("delay");
        }
    }

    public String getProgram() {
        return loadProgramFromFile(this.getItemStack(), getPlayer().getServer());
    }

    public int getDelay() {
        return this.delay;
    }
    public void setProgram(String program) {
        this.program = program;
        ProgramExpander.Result result = ProgramExpander.expand(program);
        this.code = result.success;
        this.getItemStack().getOrCreateTag().putBoolean("code", this.code);

        saveProgramToFile(
                getItemStack().getOrCreateTag().contains("program_id")
                ? this.getItemStack().getOrCreateTag().getString("program_id")
                : UUID.randomUUID().toString(),
                program,
                getPlayer().getServer()
        );
    }
    public void setDelay(int delay) {
        this.delay = delay;
        this.getItemStack().getOrCreateTag().putInt("delay", delay);
    }

    public static String loadProgramFromFile(ItemStack stack, MinecraftServer server) {
        if (!stack.hasTag() || !stack.getTag().contains("program_id")) return "";

        String id = stack.getTag().getString("program_id");
        Path file = server.getWorldPath(new LevelResource("serverdata"))
                .resolve("autobuilder")
                .resolve(id);

        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtils.getLogger().info(e.toString());
            return "";
        }
    }

    public static void saveProgramToFile(String id, String code, MinecraftServer server) {
        Path file = server.getWorldPath(new LevelResource("serverdata"))
                .resolve("autobuilder")
                .resolve(id);

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, code, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtils.getLogger().info(e.toString());
        }
    }
}
