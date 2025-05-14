package net.oktawia.crazyae2addons.interfaces;

import appeng.api.crafting.IPatternDetails;
import appeng.me.cluster.implementations.CraftingCPUCluster;

public interface IPatternProviderCpu {
    void setCpuCluster(CraftingCPUCluster cpu);
    CraftingCPUCluster getCpuCluster();
    void setPatternDetails(IPatternDetails iPatternDetails);
    IPatternDetails getPatternDetails();
}
