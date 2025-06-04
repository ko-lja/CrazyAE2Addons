package net.oktawia.crazyae2addons.misc;

import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.nbt.CompoundTag;

public class NBTPriorityList implements IPartitionList {

    private final String criteria;

    public NBTPriorityList(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public boolean isListed(AEKey input) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    @Override
    public Iterable<AEKey> getItems() {
        return null;
    }

    @Override
    public boolean matchesFilter(AEKey key, IncludeExclude mode) {
        if (key instanceof AEItemKey ik){
            return NBTMatcher.doesItemMatch(ik, criteria);
        }
        return false;
    }
}
