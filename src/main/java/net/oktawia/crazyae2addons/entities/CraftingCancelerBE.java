package net.oktawia.crazyae2addons.entities;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.CraftingCancelerMenu;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class CraftingCancelerBE extends AENetworkBlockEntity implements MenuProvider, IGridTickable, IUpgradeableObject {
    private boolean enabled;
    private int duration;
    private List<ICraftingCPU> craftingCpus;
    private Instant intervalStart;

    public CraftingCancelerBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.duration = 0;
        this.enabled = false;
        this.getMainNode().setIdlePowerUsage(4.0F).addService(IGridTickable.class, this).setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(20, 20, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode iGridNode, int ticksSinceLastCall) {
        if (!enabled || duration == 0) {
            return TickRateModulation.IDLE;
        }

        long now = Instant.now().getEpochSecond();
        long elapsed = now - intervalStart.getEpochSecond();
        if (elapsed < duration) {
            return TickRateModulation.IDLE;
        }

        List<ICraftingCPU> currentCpus = getCraftingCpus();
        List<ICraftingCPU> cpusToReset = new ArrayList<>();

        if (this.craftingCpus != null && !this.craftingCpus.isEmpty()) {
            for (ICraftingCPU cpu : currentCpus) {
                if (cpu.getJobStatus() != null) {
                    Optional<ICraftingCPU> prevCpu = this.craftingCpus.stream()
                            .filter(c -> c == cpu)
                            .findFirst();
                    if (prevCpu.isPresent() && prevCpu.get().getJobStatus() != null) {
                        if (cpu.getJobStatus().progress() == prevCpu.get().getJobStatus().progress() &&
                                cpu.getJobStatus().crafting().equals(prevCpu.get().getJobStatus().crafting())) {
                            cpusToReset.add(cpu);
                        }
                    }
                }
            }
        }

        cpusToReset.forEach(this::resetCraft);

        intervalStart = Instant.now();
        this.craftingCpus = List.copyOf(currentCpus);

        return TickRateModulation.IDLE;
    }

    private void resetCraft(ICraftingCPU cpu) {
        AEKey item = cpu.getJobStatus().crafting().what();
        long amount = cpu.getJobStatus().crafting().amount();
        cpu.cancelJob();

        ICraftingSimulationRequester simRequester = () -> new MachineSource(getGridNode().getGrid()::getPivot);
        ICraftingService craftingService = getMainNode().getGrid().getService(ICraftingService.class);
        Future<ICraftingPlan> futurePlan = craftingService.beginCraftingCalculation(
                getMainNode().getNode().getLevel(),
                simRequester,
                item,
                amount,
                CalculationStrategy.REPORT_MISSING_ITEMS);

        Utils.asyncDelay(() -> CompletableFuture.runAsync(() -> {
            try {
                ICraftingPlan plan = futurePlan.get();
                getMainNode().getGrid().getCraftingService().submitJob(
                        plan, null, null, true, simRequester.getActionSource());
            } catch (Exception e) {
                LogUtils.getLogger().error(e.getMessage());
            }
        }), 5);
    }

    @Override
    public void clearRemoved(){
        scheduleInit();
    }

    @Override
    public void onReady() {
        super.onReady();
        intervalStart = Instant.now();
        craftingCpus = List.copyOf(getCraftingCpus());
        CompoundTag data = getPersistentData();
        if(data.contains("duration")){
            this.setDuration(data.getInt("duration"));
        }
        if(data.contains("enabled")){
            this.setEnabled(data.getBoolean("enabled"));
        }
    }

    public List<ICraftingCPU> getCraftingCpus(){
        try {
            return this.getMainNode().getGrid().getCraftingService().getCpus().stream().toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CraftingCancelerMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crafting Canceler");
    }

    public void setEnabled(boolean state){
        enabled = state;
        CompoundTag data = getPersistentData();
        data.putBoolean("enabled", state);
        setChanged();
    }

    public void setDuration(int newDuration){
        duration = newDuration;
        setChanged();
        CompoundTag data = getPersistentData();
        data.putInt("duration", newDuration);
        setChanged();
    }

    public boolean getEnabled(){
        return enabled;
    }

    public int getDuration(){
        return duration;
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.CRAFTING_CANCELER_MENU, player, locator);
    }
}
