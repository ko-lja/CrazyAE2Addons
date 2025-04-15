package net.oktawia.crazyae2addons.misc;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class KeyCounterSerializer {

    public static ListTag serialize(KeyCounter[] counters) {
        ListTag main = new ListTag();
        for (KeyCounter counter : counters){
            ListTag counterTag = new ListTag();
            for (Object2LongMap.Entry<AEKey> key : counter){
                CompoundTag keyTag = new CompoundTag();
                keyTag.put("key", key.getKey().toTagGeneric());
                keyTag.putLong("counter", key.getLongValue());
                counterTag.add(keyTag);
            }
            main.add(counterTag);
        }
        return main;
    }

    public static KeyCounter[] deserialize(ListTag main) {
        KeyCounter[] counters = new KeyCounter[main.size()];
        for (int i = 0; i < main.size(); i++) {
            ListTag counterTag = (ListTag) main.get(i);
            KeyCounter counter = new KeyCounter();
            for (int j = 0; j < counterTag.size(); j++) {
                CompoundTag keyTag = counterTag.getCompound(j);
                AEKey key = AEKey.fromTagGeneric((CompoundTag) keyTag.get("key"));
                long count = keyTag.getLong("counter");
                counter.add(key, count);
            }
            counters[i] = counter;
        }
        return counters;
    }
}
