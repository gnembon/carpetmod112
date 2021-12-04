package test;

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

//    @Override
//    public V get(long k) {
//        if (((k) == (0))) return containsNullKey ? value[n] : defRetValue;
//        long curr;
//        final long[] key = this.key;
//        int pos;
//        // The starting point.
//        if (((curr = key[pos = (int) it.unimi.dsi.fastutil.HashCommon.mix((k)) & mask]) == (0))) return defRetValue;
//        if (((k) == (curr))) return value[pos];
//        // There's always an unused entry.
//        while (true) {
//            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return defRetValue;
//            if (((k) == (curr))) return value[pos];
//        }
//    }
}
