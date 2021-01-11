package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockBeacon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class Custom<T> extends Long2ObjectOpenHashMap<T> {

    long chunks = 10;

    public Custom(int size){
        super(size);
    }

    public void printstuff(){
        System.out.println("Keys " + mask);
        for(long l : key){
            System.out.println(HashCommon.mix(l));
        }
        System.out.println("Values");
        for(Object l : value){
            System.out.println(l);
        }
    }

    public T remove(final long k, boolean check) {
        if (((k) == (0))) {
            if (containsNullKey) return removeNullEntry();
            return defRetValue;
        }
        long curr;
        final long[] key = this.key;
        int pos;
        // The starting point.
        if (((curr = key[pos = (int) it.unimi.dsi.fastutil.HashCommon.mix((k)) & mask]) == (0))) return defRetValue;
        if (((k) == (curr))) return removeEntry(pos, check);
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return defRetValue;
            if (((k) == (curr))) return removeEntry(pos, check);
        }
    }

    private T removeEntry(int pos, boolean check) {
        T oldValue = this.value[pos];
        this.value[pos] = null;
        --this.size;
        this.shiftKeys2(pos, check);
        if (this.size < this.maxFill / 4 && this.n > 16) {
        }

        return oldValue;
    }

    protected final void shiftKeys2(int pos, boolean check) {
        long[] key = this.key;

        while(true) {
            int last = pos;
            pos = pos + 1 & this.mask;

            int counter = 0;
            long curr;
            while(true) {
                counter++;
                if ((curr = key[pos]) == 0L) {
                    key[last] = 0L;
                    this.value[last] = null;
                    return;
                }

                int slot = (int)HashCommon.mix(curr) & this.mask;
                if (last <= pos) {
                    if (last >= slot || slot > pos) {
                        break;
                    }
                } else if (last >= slot && slot > pos) {
                    break;
                }

                pos = pos + 1 & this.mask;
            }

            key[last] = curr;
            if(check) {
                if (BlockBeacon.aliveRegion) BlockBeacon.alive++;
                else BlockBeacon.dead++;
                if(BlockBeacon.job == 1) System.out.println("Alive or Dead: " + BlockBeacon.alive + " " + BlockBeacon.dead + " " + ((double)BlockBeacon.alive / (BlockBeacon.alive + BlockBeacon.dead))+ " " + counter);
            }
            this.value[last] = this.value[pos];
        }
    }

    public T put(long k, T v) {
        int pos = this.insert(k, v);
        if (pos < 0) {
            return this.defRetValue;
        } else {
            T oldValue = this.value[pos];
            this.value[pos] = v;
            return oldValue;
        }
    }

    private int insert(long k, T v) {
        int pos;
        if (k == 0L) {
            if (this.containsNullKey) {
                return this.n;
            }

            this.containsNullKey = true;
            pos = this.n;
        } else {
            long[] key = this.key;
            long curr;
            if ((curr = key[pos = (int)HashCommon.mix(k) & this.mask]) != 0L) {
                if (curr == k) {
                    return pos;
                }

                while((curr = key[pos = pos + 1 & this.mask]) != 0L) {
                    if (curr == k) {
                        return pos;
                    }
                }
            }
        }

        this.key[pos] = k;
        this.value[pos] = v;

        return -1;
    }

    public T get(long k) {
        if (k == 0L) {
            return this.containsNullKey ? this.value[this.n] : this.defRetValue;
        } else {
            long[] key = this.key;
            long curr;
            int pos;
            if ((curr = key[pos = (int) HashCommon.mix(k) & this.mask]) == 0L) {
                return this.defRetValue;
            } else if (k == curr) {
                return this.value[pos];
            } else {
                BlockBeacon.aliveRegion = true;
                if(BlockBeacon.counter % 10000 == 0) {
                    BlockBeacon.time2 = System.nanoTime();
                }
                while((curr = key[pos = pos + 1 & this.mask]) != 0L) {
                    if (k == curr) {
                        BlockBeacon.aliveRegion = false;
                        if(BlockBeacon.counter % 1000 == 0) {
                            BlockBeacon.time3 = System.nanoTime();
                        }
                        return this.value[pos];
                    }
                }
                if(BlockBeacon.counter % 10000 == 0) {
                    BlockBeacon.time3 = System.nanoTime();
                }
                BlockBeacon.aliveRegion = false;
                return this.defRetValue;
            }
        }
    }

    private T removeNullEntry() {
        containsNullKey = false;
        final T oldValue = value[n];
        value[n] = null;
        size--;
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
        return oldValue;
    }

    public void copySetup(Long2ObjectOpenHashMap<Chunk> loadedChunks) {
        int length = ((T[])loadedChunks.value).length;

        key = loadedChunks.key.clone();

        int i = 0;
        for(long k : key){
            System.out.println(i + " : " + (HashCommon.mix(k) & this.mask) + " : (" + loadedChunks.get(k).x + " " + loadedChunks.get(k).z + ")");
            i++;
        }

        value = (T[]) new Object[length];
        mask = loadedChunks.mask;
        n = loadedChunks.n;
        maxFill = loadedChunks.maxFill;
        size = loadedChunks.size;
    }
}
