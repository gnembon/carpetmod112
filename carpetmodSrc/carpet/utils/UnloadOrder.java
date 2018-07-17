package carpet.utils;

import carpet.CarpetSettings;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
            return UnloadOrder.tick_reportive_no_action(server, pos, verbose);
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
            Field field = chunkproviderserver.droppedChunksSet.getClass().getDeclaredField("map");
            field.setAccessible(true);
            HashMap map = (HashMap<Object,Object>)field.get(chunkproviderserver.droppedChunksSet);
            field = map.getClass().getDeclaredField("table");
            field.setAccessible(true);
            Object [] table = (Object [])field.get(map);
            if (table==null)
                return 2;
            return table.length;
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return -1;
    }


    public static int getChunkOrder(ChunkPos chpos, int hashsize)
    {
        //return HashMap_hash(Long.hashCode(ChunkPos.asLong(chpos.chunkXPos, chpos.chunkZPos)));
        try
        {
            Method method = HashMap.class.getDeclaredMethod("hash", Object.class);
            method.setAccessible(true);
            return (Integer) method.invoke(null, Long.hashCode(ChunkPos.asLong(chpos.x, chpos.z))) & (hashsize-1);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            CarpetSettings.LOG.error("You broke java");
            return -1;
        }
    }
    public static int get_chunk_order_113(ChunkPos chpos)
    {
        return HashCommon.murmurHash3(Long.hashCode(ChunkPos.asLong(chpos.x, chpos.z))) & (2<<10-1);
    }

    public static List<String> check_unload_order(WorldServer server, BlockPos pos, BlockPos pos1)
    {
        List<String> rep = new ArrayList<String>();
        int size = getCurrentHashSize(server);
        if (pos1 == null)
        {
            ChunkPos chpos = new ChunkPos(pos);
            int o = getChunkOrder(chpos, size);
            rep.add("Chunks order of "+chpos+" is "+o+" / "+size);//+", or "+Integer.toBinaryString(o));
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
                int o1 = getChunkOrder(new ChunkPos(chposx, chposz),size);
                int count = stat.containsKey(o1) ? stat.get(o1) : 0;
                stat.put(o1, count + 1);
                total ++;
            }
        }
        rep.add("Counts of chunks with specific unload order / "+size+" ("+total+" total)");
        SortedSet<Integer> keys = new TreeSet<Integer>(stat.keySet());
        for (int key : keys)
        {
            rep.add(" - order "+key+": "+stat.get(key));

        }
        return rep;

    }

    /* carpet mod */
    public static String stringify_chunk_id(ChunkProviderServer provider, int index, Long olong, int size)
    {
        Chunk chunk = (Chunk)provider.id2ChunkMap.get(olong);

        return String.format(" - %4d: (%d, %d) at X %d, Z %d (order: %d / %d)",
                index+1,
                chunk.x, chunk.z,
                chunk.x * 16+7, chunk.z*16+7,
                UnloadOrder.getChunkOrder(new ChunkPos(chunk.x, chunk.z), size),
                size
        );
    }
    /* carpet mod */
    public static List<String> tick_reportive_no_action(WorldServer world, BlockPos pos, boolean verbose)
    {
        ChunkProviderServer provider = world.getChunkProvider();
        List<String> rep = new ArrayList<String>();
        int test_chunk_xpos = 0;
        int test_chunk_zpos = 0;
        if (pos != null)
        {
            test_chunk_xpos = pos.getX() >> 4;
            test_chunk_zpos = pos.getZ() >> 4;
        }
        int current_size = UnloadOrder.getCurrentHashSize(world);
        if (!world.disableLevelSaving)
        {
            if (!provider.droppedChunksSet.isEmpty())
            {
                int total_chunks = provider.droppedChunksSet.size();
                Iterator<Long> iterator = provider.droppedChunksSet.iterator();
                List<Long> chunks_ids_order = new ArrayList<Long>();
                int selected_chunk = -1;
                int iti = 0;
                int i = 0;
                for (i = 0; iterator.hasNext(); iterator.remove())
                {
                    Long olong = (Long)iterator.next();
                    Chunk chunk = (Chunk)provider.id2ChunkMap.get(olong);

                    if (chunk != null && chunk.unloadQueued)
                    {
                        int chunk_xpos = chunk.x * 16;
                        int chunk_zpos = chunk.z * 16;
                        if ( pos != null && chunk.x == test_chunk_xpos && chunk.z == test_chunk_zpos)
                        {
                            selected_chunk = i;
                        }
                        chunks_ids_order.add(olong);
                        ++i;
                    }
                    ++iti;
                }
                if (i != iti)
                {
                    rep.add("There were some ineligible chunks to be unloaded,");
                    rep.add("so the actual 100 chunk mark might not be accurate");
                }
                int total = chunks_ids_order.size();
                List<Integer> llll = Arrays.asList(0, 1,2, -1, 97, 98, 99, -2, 100, 101, 102, -1,
                        total-3, total-2, total-1);
                if (total <= 100)
                {
                    rep.add(String.format("There is only %d chunks to unload, all will be unloaded", total));
                    if (total > 5)
                    {
                        llll = Arrays.asList(0, 1, -1,total-2, total -1 );
                    }
                    else
                    {
                        llll = Arrays.asList(-2);
                    }
                }
                if (verbose)
                {
                    for (int iii=0; iii<chunks_ids_order.size(); iii++)
                    {
                        rep.add(stringify_chunk_id(provider, iii, chunks_ids_order.get(iii), current_size));
                    }
                }
                else
                {
                    for (int idx: llll)
                    {
                        if (idx < 0)
                        {
                            if (idx == -1)
                            {
                                rep.add("    ....");
                            }
                            else
                            {
                                rep.add("--------");
                            }
                        }
                        else
                        {
                            if (idx >= total)
                            {
                                continue;
                            }
                            rep.add(stringify_chunk_id(provider, idx, chunks_ids_order.get(idx), current_size));
                        }
                    }
                }
                if (pos != null)
                {

                    if (selected_chunk == -1)
                    {
                        rep.add("Selected chunk was not marked for unloading");
                    }
                    else
                    {
                        rep.add(String.format("Selected chunk was %d on the list", selected_chunk+1) );
                    }
                }
            }
            else
            {
                rep.add("There are no chunks to get unloaded");
            }
        }
        else
        {
            rep.add("Level Saving is disabled.");
        }
        return rep;
    }
}
