package accuratetimer;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayList;
import java.util.List;

public class LastQuestInCode {
    public static boolean enabled = false;

    public static void main(String[] args) {
        enabled = true;
        MinecraftServer.main(args);
    }

    public static boolean SHOULDB_REAK_ON_LOAD = false;
    static AbstractLong2ObjectMap<Chunk> chunkMap;
    static ChunkProviderServer chunkService;
    public static volatile long TIME_STATE = 0;
    public static long COUNT = 0;
    public static void hook(World overworld) throws Exception {
        chunkService = (ChunkProviderServer)overworld.getChunkProvider();
        chunkMap = (AbstractLong2ObjectMap<Chunk>)(chunkService).loadedChunks;

        int glassX = 0;
        int glassZ = 0;
        long m = 0;
        while (true) {
            m++;
            long commbined = HashCommon.invMix(0b10000000000000000000000L | m << 23);
            int x = (int) (commbined & 4294967295L);
            int z = (int) ((commbined >> 32) & 4294967295L);
            if (!(x < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < x &&
                    z < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < z))
                continue;
            glassX = x;
            glassZ = z;
            break;
        }

        int rehashX = 0;
        int rehashZ = 0;

        m = 99999999999999L;
        while (true) {
            m++;
            long commbined = HashCommon.invMix(0b10000000000000000000000L | m << 17);
            int x = (int) (commbined & 4294967295L);
            int z = (int) ((commbined >> 32) & 4294967295L);
            if (!(x < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < x &&
                    z < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < z))
                continue;
            rehashX = x;
            rehashZ = z;
            break;
        }



        int bitLevel = 12;
        int InitalSize = 1<<(bitLevel);
        int targetFill = HashCommon.maxFill(InitalSize, 0.75f)-1 + 1 + 1;
        List<ChunkPos> chunksLoaded = new ArrayList<>();
        for (long q = 1; q < (targetFill- 1) - chunkMap.size() ; q++) {

            m = q*1000000;
            while (true) {
                m++;
                long commbined = HashCommon.invMix(m<<(bitLevel+1) | (1<<bitLevel));
                int x = (int) (commbined & 4294967295L);
                int z = (int) ((commbined >> 32) & 4294967295L);
                if (!(x < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < x &&
                        z < Integer.MAX_VALUE / 16 && Integer.MIN_VALUE / 16 < z))
                    continue;
                break;
            }


            long commbined = HashCommon.invMix(m<<bitLevel | (1<<bitLevel));
            int x = (int)(commbined&4294967295L);
            int z = (int)((commbined>>32)&4294967295L);
            chunksLoaded.add(new ChunkPos(x,z));
        }



        //Force chunk map cleanup
        chunkService.provideChunk(1000,1000);
        chunkMap.remove(ChunkPos.asLong(1000,1000));
        //Again
        chunkService.provideChunk(1000,1000);
        chunkMap.remove(ChunkPos.asLong(1000,1000));
        //Again
        chunkService.provideChunk(1000,1000);
        chunkMap.remove(ChunkPos.asLong(1000,1000));



        Chunk PRELOADED = chunkService.provideChunk(rehashX, rehashZ);
        chunkMap.remove(ChunkPos.asLong(rehashX,rehashZ));


        while (true) {
            SHOULDB_REAK_ON_LOAD = false;
            Chunk TARGET = chunkService.generateChunk(glassX, glassZ);
            chunkMap.remove(ChunkPos.asLong(glassX, glassZ));
            SHOULDB_REAK_ON_LOAD = true;


            COUNT++;
            TIME_STATE = 0;
            //System.out.println("TICK");

            //Load the cluster
            for (int q = 0; q < chunksLoaded.size(); q++) {
                chunkMap.put(ChunkPos.asLong(chunksLoaded.get(q).x, chunksLoaded.get(q).z), null);
            }

            //Add lazily unloaded target
            chunkMap.put(ChunkPos.asLong(glassX, glassZ), TARGET);
            chunkMap.get(ChunkPos.asLong(glassX, glassZ));
            //Boot up all the bloody glass threads
            for (int i = 0; i < 300; i++) {
                BlockPos glass = new BlockPos((i % 16) + glassX * 16, 255, ((i / 16)%16) + glassZ * 16);
                //BlockBeacon.updateColorAsync(overworld, glass);
                TARGET.setBlockState(glass, Blocks.STAINED_GLASS.getDefaultState());

            }


            long b = System.currentTimeMillis();
            while ((System.currentTimeMillis() - b) < 2);
            chunkService.loadChunkFromFile(rehashX, rehashZ);


            chunkMap.put(ChunkPos.asLong(rehashX, rehashZ), PRELOADED);
            TIME_STATE = System.nanoTime();
            System.out.flush();

            Thread.sleep(200);
            for (ChunkPos p : chunksLoaded) {
                chunkMap.remove(ChunkPos.asLong(p.x, p.z));
            }
            chunkMap.remove(ChunkPos.asLong(rehashX, rehashZ));
            chunkMap.remove(ChunkPos.asLong(glassX, glassZ));
        }
        //System.exit(-1);
    }

    public static void onChunkLoad() {
        if (SHOULDB_REAK_ON_LOAD) {
            Thread.dumpStack();
            System.err.println("Took: " + COUNT + " attempts");
            COUNT = 0;
        }
    }
}
