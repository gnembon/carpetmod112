package carpet.helpers;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class CustomHashMap<V> extends Long2ObjectOpenHashMap<V> {
    public CustomHashMap(int expected) {
        super(expected);
    }

    public long[] getKey() {
        return key;
    }

    public V[] getValues() {
        return value;
    }

    public int getHashSize() {
        return n;
    }
}