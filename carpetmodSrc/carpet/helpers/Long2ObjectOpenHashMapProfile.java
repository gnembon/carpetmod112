package carpet.helpers;

import carpet.utils.CarpetProfiler;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class Long2ObjectOpenHashMapProfile<V> extends Long2ObjectOpenHashMap<V> {

    public Long2ObjectOpenHashMapProfile(int expected) {
        super(expected, 0.75F);
    }

    @Override
    public V get(long k) {
        if(!CarpetProfiler.lastQuestProfiler) return super.get(k);
        if(CarpetProfiler.mainThread.get()) return super.get(k);

        if (k == 0L) {
            return this.containsNullKey ? this.value[this.n] : this.defRetValue;
        } else {
            long[] key = this.key;
            CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.GET_BEGIN);
            CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
            long curr;
            int pos;
            if ((curr = key[pos = (int) HashCommon.mix(k) & this.mask]) == 0L) {
                CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.GET_IS_EMPTY);
                CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
                return this.defRetValue;
            } else if (k == curr) {
                CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.GET_NO_COLLISION);
                CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
                return this.value[pos];
            } else {
                while((curr = key[pos = pos + 1 & this.mask]) != 0L) {
                    if (k == curr) {
                        CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.GET_COLLISION);
                        CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
                        return this.value[pos];
                    }
                }
                CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.GET_COLLISION_FINISHED);
                CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
                return this.defRetValue;
            }
        }
    }

    @Override
    protected void rehash(int newN) {
        if(!CarpetProfiler.lastQuestProfiler) {
            super.rehash(newN);
            return;
        }
        CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.REHASH_START);
        CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
        long[] key = this.key;
        V[] value = this.value;
        int mask = newN - 1;
        long[] newKey = new long[newN + 1];
        V[] newValue = (V[])(new Object[newN + 1]);
        int i = this.n;

        int pos;
        for(int var9 = realSize(); var9-- != 0; newValue[pos] = value[i]) {
            do {
                --i;
            } while(key[i] == 0L);

            if (newKey[pos = (int)HashCommon.mix(key[i]) & mask] != 0L) {
                while(newKey[pos = pos + 1 & mask] != 0L) {
                }
            }

            newKey[pos] = key[i];
        }

        newValue[newN] = value[this.n];
        this.n = newN;
        this.mask = mask;
        CarpetProfiler.profileLastQuest.get().push(CarpetProfiler.REHASH_FINISHED);
        CarpetProfiler.profileLastQuest.get().push(System.nanoTime());
        this.maxFill = HashCommon.maxFill(this.n, this.f);
        this.key = newKey;
        this.value = newValue;
    }

    private int realSize() {
        return this.containsNullKey ? this.size - 1 : this.size;
    }
}
