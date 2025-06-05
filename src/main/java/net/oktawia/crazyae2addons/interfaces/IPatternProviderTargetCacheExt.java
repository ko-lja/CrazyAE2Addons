package net.oktawia.crazyae2addons.interfaces;

import appeng.api.crafting.IPatternDetails;
import appeng.helpers.patternprovider.PatternProviderTarget;
import net.oktawia.crazyae2addons.entities.CraftingGuardBE;

public interface IPatternProviderTargetCacheExt {
    PatternProviderTarget find(IPatternDetails patternDetails);
    void setDetails(IPatternDetails patternDetails);
    void setGuard(CraftingGuardBE guard);
    void setExclusiveMode(boolean mode);
}
