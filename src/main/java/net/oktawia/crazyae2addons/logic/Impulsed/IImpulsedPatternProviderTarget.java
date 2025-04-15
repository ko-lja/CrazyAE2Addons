package net.oktawia.crazyae2addons.logic.Impulsed;

import appeng.helpers.patternprovider.PatternProviderTarget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import appeng.api.networking.security.IActionSource;

public interface IImpulsedPatternProviderTarget extends PatternProviderTarget {
    ImpulsedPatternProviderTargetCache parent();

    default CompoundTag serialize() {
        return parent().serialize();
    }

    static IImpulsedPatternProviderTarget deserialize(CompoundTag tag, MinecraftServer server, IActionSource src) {
        return ImpulsedPatternProviderTargetCache.deserialize(tag, server, src);
    }
}
