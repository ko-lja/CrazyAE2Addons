package net.oktawia.crazyae2addons.entities;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;

import java.util.*;

public class CraftingGuardBE extends AENetworkBlockEntity implements IGridTickable {
    public Map<BlockPos, Integer> excluded = new HashMap<>();

    public CraftingGuardBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.CRAFTING_GUARD_BE.get(), pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(4.0F)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.CRAFTING_GUARD_BLOCK.get().asItem())
                );
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var grid = getMainNode().getGrid();
        if (grid != null){
            Set<CraftingGuardBE> guards = grid.getMachines(CraftingGuardBE.class);
            for (CraftingGuardBE guard : guards){
                if (!guard.getBlockPos().equals(this.getBlockPos())){
                    getLevel().destroyBlock(getBlockPos(), true);
                }
            }
        }
        if (this.getLevel() != null && this.getLevel().getServer() != null){
            int currentTick = this.getLevel().getServer().getTickCount();
            this.excluded.entrySet().removeIf(entry -> entry.getValue() != currentTick);
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        ListTag list = data.getList("excluded", Tag.TAG_LIST);

        for (Tag t : list) {
            if (t instanceof CompoundTag entryTag) {
                int x = entryTag.getInt("x");
                int y = entryTag.getInt("y");
                int z = entryTag.getInt("z");
                int tick = entryTag.getInt("tick");

                excluded.put(new BlockPos(x, y, z), tick);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data){
        super.saveAdditional(data);
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, Integer> entry : excluded.entrySet()) {
            BlockPos pos = entry.getKey();
            int tick = entry.getValue();

            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("x", pos.getX());
            entryTag.putInt("y", pos.getY());
            entryTag.putInt("z", pos.getZ());
            entryTag.putInt("tick", tick);

            list.add(entryTag);
        }
        data.put("excluded", list);
    }
}
