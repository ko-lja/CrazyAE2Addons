package net.oktawia.crazyae2addons.misc;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.io.IOException;

public class KeyCounterAdapter extends TypeAdapter<KeyCounter> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, KeyCounter keyCounter) throws IOException {
        out.beginArray();
        for (Object2LongMap.Entry<AEKey> entry : keyCounter) {
            out.beginObject();
            out.name("key");
            gson.toJson(entry.getKey(), AEKey.class, out);
            out.name("value").value(entry.getLongValue());
            out.endObject();
        }
        out.endArray();
    }

    @Override
    public KeyCounter read(JsonReader in) throws IOException {
        KeyCounter kc = new KeyCounter();
        in.beginArray();
        while (in.hasNext()) {
            in.beginObject();
            AEKey key = null;
            long value = 0;
            while (in.hasNext()) {
                String name = in.nextName();
                if ("key".equals(name)) {
                    key = gson.fromJson(in, AEKey.class);
                } else if ("value".equals(name)) {
                    value = in.nextLong();
                }
            }
            in.endObject();
            if (key != null) {
                kc.add(key, value);
            }
        }
        in.endArray();
        return kc;
    }
}
