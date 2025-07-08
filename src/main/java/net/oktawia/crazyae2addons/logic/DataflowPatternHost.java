package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DataflowPatternHost extends ItemMenuHost {
    public DataflowPatternHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
    }
}
