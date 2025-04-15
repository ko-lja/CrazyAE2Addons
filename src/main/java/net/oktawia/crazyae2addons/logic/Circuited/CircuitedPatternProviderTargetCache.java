package net.oktawia.crazyae2addons.logic.Circuited;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class CircuitedPatternProviderTargetCache {
    public final BlockApiCache<MEStorage> cache;
    public final Direction direction;
    public final IActionSource src;
    public Map<AEKeyType, ExternalStorageStrategy> strategies = new HashMap<>();
    public final BlockPos pos;
    public final ServerLevel level;

    CircuitedPatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src) {
        this.cache = BlockApiCache.create(Capabilities.STORAGE, l, pos);
        this.level = l;
        this.pos = pos;
        this.direction = direction;
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
    }

    @Nullable
    ICircuitedPatternProviderTarget find() {
        var meStorage = cache.find(direction);
        if (meStorage != null) {
            return wrapMeStorage(meStorage);
        }

        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet()) {
            var wrapper = entry.getValue().createWrapper(false, () -> {});
            if (wrapper != null) {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (!externalStorages.isEmpty()) {
            return wrapMeStorage(new CompositeStorage(externalStorages));
        }

        return null;
    }

    public ICircuitedPatternProviderTarget wrapMeStorage(MEStorage storage) {
        return new ICircuitedPatternProviderTarget() {
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) {
                        return true;
                    }
                }
                return false;
            }

            public CircuitedPatternProviderTargetCache parent(){
                return CircuitedPatternProviderTargetCache.this;
            }
        };
    }
}
