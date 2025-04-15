package net.oktawia.crazyae2addons.logic.Impulsed;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public class ImpulsedPatternProviderTargetCache {
    public final BlockApiCache<MEStorage> cache;
    public final Direction direction;
    public final IActionSource src;
    public final Map<AEKeyType, ExternalStorageStrategy> strategies;
    public final BlockPos pos;
    public final ServerLevel level;

    ImpulsedPatternProviderTargetCache(ServerLevel level, BlockPos pos, Direction direction, IActionSource src) {
        this.cache = BlockApiCache.create(Capabilities.STORAGE, level, pos);
        this.level = level;
        this.pos = pos;
        this.direction = direction;
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(level, pos, direction);
    }

    @Nullable
    IImpulsedPatternProviderTarget find() {
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

    public IImpulsedPatternProviderTarget wrapMeStorage(MEStorage storage) {
        return new IImpulsedPatternProviderTarget() {
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

            @Override
            public ImpulsedPatternProviderTargetCache parent() {
                return ImpulsedPatternProviderTargetCache.this;
            }
        };
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", level.dimension().location().toString());
        tag.putLong("pos", pos.asLong());
        tag.putString("dir", direction != null ? direction.name() : Direction.NORTH.name());
        return tag;
    }

    public static IImpulsedPatternProviderTarget deserialize(CompoundTag tag, MinecraftServer server, IActionSource src) {
        String dim = tag.getString("dim");
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        Direction dir = Direction.byName(tag.getString("dir"));
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim)));
        ImpulsedPatternProviderTargetCache cache = new ImpulsedPatternProviderTargetCache(level, pos, dir, src);
        return cache.find();
    }
}
