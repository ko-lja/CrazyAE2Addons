package net.oktawia.crazyae2addons.mixins;

import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.oktawia.crazyae2addons.CrazyConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stone.mae2.appeng.helpers.patternprovider.PatternProviderTargetCache;
import stone.mae2.parts.p2p.PatternP2PTunnel;
import appeng.api.config.Actionable;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.helpers.patternprovider.UnlockCraftingEvent;
import org.spongepowered.asm.mixin.Overwrite;
import stone.mae2.parts.p2p.PatternP2PTunnel.TunneledPatternProviderTarget;
import stone.mae2.parts.p2p.PatternP2PTunnel.TunneledPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = PatternProviderLogic.class, priority = 1050, remap = false)
public abstract class MixinGTMAE2 {

    @Shadow
    private PatternProviderLogicHost host;
    @Shadow
    private IManagedGridNode mainNode;
    @Shadow
    private IActionSource actionSource;
    @Shadow
    private ConfigManager configManager;
    @Shadow
    private int priority;
    @Shadow
    private AppEngInternalInventory patternInventory;
    @Shadow
    private List<IPatternDetails> patterns;
    @Shadow
    private Set<AEKey> patternInputs;
    // Pattern sending logic
    @Shadow
    private List<GenericStack> sendList;
    @Shadow
    private Direction sendDirection;
    // Stack returning logic
    @Shadow
    private PatternProviderReturnInventory returnInv;
    @Shadow
    private YesNo redstoneState;
    @Nullable
    @Shadow
    private UnlockCraftingEvent unlockEvent;
    @Nullable
    @Shadow
    private GenericStack unlockStack;
    @Shadow
    private int roundRobinIndex;
    private BlockPos sendPos;
    private PatternProviderTargetCache cache;

    /**
     * AE2's code is just not amenable to changes this radical, so I have to
     * overwrite it to allow multiple pattern targets per side. This is potentially
     * possible with finer grained overwrites (is that even possible?) or asking AE2
     * to change their code to allow this
     *
     * @param patternDetails
     * @param inputHolder
     * @author Stone
     * @reason Had to rewrite it to be p2p aware, The original method just isn't
     *         flexible enough to do this with usual mixins
     *
     *         Hi oktawia here, can not mixin into overwrites So I need to copy
     *         ALL and add my feature this way
     *
     * @return
     */
    @Overwrite
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive()
                || !this.patterns.contains(patternDetails))
        {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();

        if (getCraftingLockedReason() != LockCraftingMode.NONE)
        {
            return false;
        }
        for (Direction direction : getActiveSides())
        {

            Direction adjBeSide = direction.getOpposite();
            List<TunneledPos> positions = getTunneledPositions(
                    be.getBlockPos().relative(direction), level, adjBeSide);
            if (positions == null) {
                continue;
            }
            for (TunneledPos adjPos : positions)
            {
                BlockEntity adjBe = level.getBlockEntity(adjPos.pos());

                ICraftingMachine craftingMachine = ICraftingMachine.of(level, adjPos.pos(),
                        adjPos.dir(), adjBe);
                if (craftingMachine != null && craftingMachine.acceptsPlans())
                {
                    if (craftingMachine.pushPattern(patternDetails, inputHolder, adjPos.dir()))
                    {
                        CompoundTag tag = patternDetails.getDefinition().getTag();
                        int c = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;
                        setCirc(c, adjPos.pos(), adjBe.getLevel());
                        onPushPatternSuccess(patternDetails);
                        return true;
                    }
                }
            }
        }
        if (patternDetails.supportsPushInputsToExternalInventory()) {
            List<TunneledPatternProviderTarget> adapters = new ArrayList<>();
            for (Direction direction : getActiveSides())
            {
                findAdapters(be, level, adapters, direction);
            }
            rearrangeRoundRobin(adapters);

            for (TunneledPatternProviderTarget adapter : adapters)
            {
                PatternProviderTargetCache targetCache = adapter.target();
                PatternProviderTarget target = targetCache == null
                        ? findAdapter(adapter.pos().dir())
                        : targetCache.find();

                if (target == null)
                {
                    continue;
                }
                if (this.isBlocking() && target.containsPatternInput(this.patternInputs))
                {
                    continue;
                }

                if (this.adapterAcceptsAll(target, inputHolder))
                {
                    patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) ->
                    {
                        long inserted = target.insert(what, amount, Actionable.MODULATE);
                        if (inserted < amount)
                        {
                            this.addToSendList(what, amount - inserted);
                        }
                    });
                    CompoundTag tag = patternDetails.getDefinition().getTag();
                    int c = (tag != null && tag.contains("circuit")) ? tag.getInt("circuit") : 0;
                    setCirc(c, adapter.pos().pos(), host.getBlockEntity().getLevel());
                    onPushPatternSuccess(patternDetails);
                    this.sendPos = adapter.pos().pos();
                    this.sendDirection = adapter.pos().dir();
                    this.cache = targetCache;
                    ++roundRobinIndex;
                    return true;
                }
            }
        }
        return false;

    }

    @Unique
    private void findAdapters(BlockEntity be, Level level,
                              List<TunneledPatternProviderTarget> adapters, Direction direction) {
        BlockEntity potentialPart = level.getBlockEntity(be.getBlockPos().relative(direction));

        if (potentialPart == null || !(potentialPart instanceof IPartHost))
        {
            adapters.add(new TunneledPatternProviderTarget(null,
                    new TunneledPos(be.getBlockPos(), direction)));
        } else
        {
            IPart potentialTunnel = ((IPartHost) potentialPart).getPart(direction.getOpposite());
            if (potentialTunnel != null && potentialTunnel instanceof PatternP2PTunnel)
            {
                List<TunneledPatternProviderTarget> newTargets = ((PatternP2PTunnel) potentialTunnel)
                        .getTargets();
                if (newTargets != null)
                {
                    adapters.addAll(newTargets);
                }
            } else
            {
                adapters.add(new TunneledPatternProviderTarget(null,
                        new TunneledPos(be.getBlockPos(), direction)));
            }
        }
    }

    @Unique
    private boolean sendStacksOut(PatternProviderTarget adapter) {
        if (adapter == null)
        {
            return false;
        }

        for (var it = sendList.listIterator(); it.hasNext();)
        {
            var stack = it.next();
            var what = stack.what();
            long amount = stack.amount();

            long inserted = adapter.insert(what, amount, Actionable.MODULATE);
            if (inserted >= amount)
            {
                it.remove();
                return true;
            } else if (inserted > 0)
            {
                it.set(new GenericStack(what, amount - inserted));
                return true;
            }
        }

        if (sendList.isEmpty())
        {
            sendPos = null;
        }

        return false;
    }

    /**
     * AE2 uses this method to send out ingredients that couldn't fit all at once
     * and have to be put in as space is made. I had to change it to use a cached
     * position found when the initial pattern was pushed. The position already has
     * gone through potential p2p tunnels.
     *
     * @author Stone
     * @reason This method needs to be aware of the pattern p2p, and the original
     *         isn't flexible enough to allow that
     * @return true if it succeeded pushing out stacks
     */
    @Overwrite
    private boolean sendStacksOut() {
        if (sendDirection == null)
        {
            if (!sendList.isEmpty())
            {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }

        if (cache == null)
        {
            if (this.sendPos == null)
            {
                return sendStacksOut(findAdapter(this.sendDirection));
            } else
            {
                // when crafts are saved through a load, the cache won't exist but the send pos
                // will
                this.cache = findCache(sendPos, sendDirection);
            }
        }
        return sendStacksOut(cache.find());
    }

    @Unique
    private List<TunneledPos> getTunneledPositions(BlockPos pos, Level level, Direction adjBeSide) {
        BlockEntity potentialPart = level.getBlockEntity(pos);
        if (potentialPart == null || !(potentialPart instanceof IPartHost))
        {
            // can never tunnel
            return List.of(new TunneledPos(pos, adjBeSide));
        } else
        {
            IPart potentialTunnel = ((IPartHost) potentialPart).getPart(adjBeSide);
            if (potentialTunnel instanceof PatternP2PTunnel tunnel)
            {
                return tunnel.getTunneledPositions();
            } else
            {
                // not a pattern p2p tunnel
                return List.of(new PatternP2PTunnel.TunneledPos(pos, adjBeSide));
            }
        }
    }

    @Nullable
    private PatternProviderTargetCache findCache(BlockPos pos, Direction dir) {
        var thisBe = host.getBlockEntity();
        return new PatternProviderTargetCache((ServerLevel) thisBe.getLevel(), pos, dir, actionSource);
    }

    @Shadow
    private PatternProviderTarget findAdapter(Direction side) {
        throw new RuntimeException("HOW, HOW DID YOU LOAD THIS!");
    };

    private static final String SEND_POS_TAG = "sendPos";

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void onWriteToNBT(CompoundTag tag, CallbackInfo ci) {
        if (sendPos != null)
        {
            tag.putLong(SEND_POS_TAG, sendPos.asLong());
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadFromNBT(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(SEND_POS_TAG))
        // send pos only exists if MAE2 existed before
        {
            Tag sendPosTag = tag.get(SEND_POS_TAG);
            if (sendPosTag instanceof NumericTag numericTag) {
                sendPos = BlockPos.of(numericTag.getAsLong());
            } else if (sendPosTag instanceof CompoundTag compoundTag) {
                PatternP2PTunnel.TunneledPos tunnelPos = PatternP2PTunnel.TunneledPos.readFromNBT(compoundTag);
                this.sendPos = tunnelPos.pos();
                this.sendDirection = tunnelPos.dir();
            }
        }
    }

    @Shadow
    private <T> void rearrangeRoundRobin(List<T> list) {}

    @Shadow
    public abstract boolean isBlocking();

    @Shadow
    private boolean adapterAcceptsAll(PatternProviderTarget adapter, KeyCounter[] inputHolder) {
        return false;
    }

    @Shadow
    private void addToSendList(AEKey what, long l) {}

    @Shadow
    private void onPushPatternSuccess(IPatternDetails patternDetails) {}

    @Shadow
    private Set<Direction> getActiveSides() {
        return null;
    }

    @Shadow
    public abstract LockCraftingMode getCraftingLockedReason();


    @Unique
    private static void setCirc(int circ, BlockPos pos, Level lvl){
        if (!CrazyConfig.COMMON.enableCPP.get()) return;
        try {
            var machine = SimpleTieredMachine.getMachine(lvl, pos);
            NotifiableItemStackHandler inv;
            if (machine instanceof SimpleTieredMachine STM){
                inv = STM.getCircuitInventory();
            } else if (machine instanceof ItemBusPartMachine IBPM) {
                inv = IBPM.getCircuitInventory();
            } else if (machine instanceof FluidHatchPartMachine FHPM) {
                inv = FHPM.getCircuitInventory();
            } else {
                return;
            }
            if (inv.getSlots() == 0) {
                return;
            }
            if (circ == 0){
                inv.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                var machineStack = GTItems.PROGRAMMED_CIRCUIT.asStack();
                IntCircuitBehaviour.setCircuitConfiguration(machineStack, circ);
                inv.setStackInSlot(0, machineStack);
            }
        } catch (Throwable e) {
            LogUtils.getLogger().info(e.toString());
        }
    }
}
