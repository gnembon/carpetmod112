package carpet.utils;

import carpet.CarpetSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.HashCommon;

public class UnloadOrder
{
    public static List<String> test_save_chunks(WorldServer server, BlockPos pos, boolean verbose)
    {

        ChunkProviderServer chunkproviderserver = server.getChunkProvider();

        if (chunkproviderserver.canSave())
        {

            chunkproviderserver.saveChunks(true);

            PlayerChunkMap pcm = server.getPlayerChunkMap();

            for (Chunk chunk : Lists.newArrayList(chunkproviderserver.getLoadedChunks()))
            {
                if (chunk != null && !pcm.contains(chunk.x, chunk.z))
                {
                    chunkproviderserver.queueUnload(chunk);
                }
            }
            return chunkproviderserver.tick_reportive_no_action(pos, verbose);
        }
        List<String> rep = new ArrayList<String>();
        rep.add("Saving is disabled on the server");
        return rep;
    }
    public static int getCurrentHashSize(WorldServer server)
    {
        ChunkProviderServer chunkproviderserver = server.getChunkProvider();
        try
        {
            Field field = chunkproviderserver.droppedChunksSet.getClass().getDeclaredField("table");
            //WIP
            return 0;
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return -1;
    }


    public static int get_chunk_order(ChunkPos chpos)
    {
        //return HashMap_hash(Long.hashCode(ChunkPos.asLong(chpos.chunkXPos, chpos.chunkZPos)));
        try
        {
            Method method = HashMap.class.getDeclaredMethod("hash", Object.class);
            method.setAccessible(true);
            return (Integer) method.invoke(null, Long.hashCode(ChunkPos.asLong(chpos.x, chpos.z))) & (4096-1);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            CarpetSettings.LOG.error("You broke java");
            return -1;
        }
    }
    public static int get_chunk_order_new(ChunkPos chpos)
    {
        //return HashMap_hash(Long.hashCode(ChunkPos.asLong(chpos.chunkXPos, chpos.chunkZPos)));
        return HashCommon.murmurHash3(Long.hashCode(ChunkPos.asLong(chpos.x, chpos.z))) % (2<<20);

    }

    public static List<String> check_unload_order(BlockPos pos, BlockPos pos1)
    {
        List<String> rep = new ArrayList<String>();
        if (pos1 == null)
        {
            ChunkPos chpos = new ChunkPos(pos);
            int o = get_chunk_order(chpos);
            rep.add("Chunks order of "+chpos+" is "+o);//+", or "+Integer.toBinaryString(o));
            return rep;
        }
        ChunkPos chpos1 = new ChunkPos(pos);
        ChunkPos chpos2 = new ChunkPos(pos1);
        int minx = (chpos1.x < chpos2.x) ? chpos1.x : chpos2.x;
        int maxx = (chpos1.x > chpos2.x) ? chpos1.x : chpos2.x;
        int minz = (chpos1.z < chpos2.z) ? chpos1.z : chpos2.z;
        int maxz = (chpos1.z > chpos2.z) ? chpos1.z : chpos2.z;
        HashMap<Integer,Integer> stat = new HashMap<Integer,Integer>();
        int total = 0;
        for (int chposx = minx; chposx <= maxx; chposx++)
        {
            for (int chposz = minz; chposz <= maxz; chposz++)
            {
                int o1 = get_chunk_order(new ChunkPos(chposx, chposz));
                int count = stat.containsKey(o1) ? stat.get(o1) : 0;
                stat.put(o1, count + 1);
                total ++;
            }
        }
        rep.add("Counts of chunks with specific unload order ("+total+" total)");
        SortedSet<Integer> keys = new TreeSet<Integer>(stat.keySet());
        for (int key : keys)
        {
            rep.add(" - order "+key+": "+stat.get(key));

        }
        return rep;

    }
}
