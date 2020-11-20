package carpet.utils;

import carpet.CarpetSettings;
import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.processing.Messager;
import java.util.*;
import java.util.stream.Collectors;

public class CarpetProfiler
{
    private static final HashMap<String, Long> time_repo = new HashMap<String, Long>();
    public static int tick_health_requested = 0;
    private static int tick_health_elapsed = 0;
    private static int test_type = 0; //1 for ticks, 2 for entities;
    private static String current_section = null;
    private static long current_section_start = 0;
    private static long current_tick_start = 0;

    public static void prepare_tick_report(int ticks)
    {
        //maybe add so it only spams the sending player, but honestly - all may want to see it
        time_repo.clear();
        test_type = 1;
        time_repo.put("tick",0L);
        time_repo.put("Network",0L);
        time_repo.put("Autosave",0L);

        time_repo.put("overworld.spawning",0L);
        time_repo.put("overworld.blocks",0L);
        time_repo.put("overworld.entities",0L);
        time_repo.put("overworld.tileentities",0L);

        time_repo.put("the_nether.spawning",0L);
        time_repo.put("the_nether.blocks",0L);
        time_repo.put("the_nether.entities",0L);
        time_repo.put("the_nether.tileentities",0L);

        time_repo.put("the_end.spawning",0L);
        time_repo.put("the_end.blocks",0L);
        time_repo.put("the_end.entities",0L);
        time_repo.put("the_end.tileentities",0L);
        //spawning
        //blocks
        //entities
        //tileentities

        tick_health_elapsed = ticks;
        tick_health_requested = ticks;
        current_tick_start = 0L;
        current_section_start = 0L;
        current_section = null;

    }

    public static void start_section(String dimension, String name)
    {
        if (tick_health_requested == 0L || test_type != 1)
        {
            return;
        }
        if (current_tick_start == 0L)
        {
            return;
        }
        if (current_section != null)
        {
            end_current_section();
        }
        String key = name;
        if (dimension != null)
        {
            key = dimension+"."+name;
        }
        current_section = key;
        current_section_start = System.nanoTime();
    }

    public static void start_entity_section(String dimension, Entity e)
    {
        if (tick_health_requested == 0L || test_type != 2)
        {
            return;
        }
        if (current_tick_start == 0L)
        {
            return;
        }
        if (current_section != null)
        {
            end_current_section();
        }
        current_section = dimension+"."+e.cm_name();
        current_section_start = System.nanoTime();
    }

    public static void start_tileentity_section(String dimension, TileEntity e)
    {
        if (tick_health_requested == 0L || test_type != 2)
        {
            return;
        }
        if (current_tick_start == 0L)
        {
            return;
        }
        if (current_section != null)
        {
            end_current_section();
        }
        current_section = dimension+"."+e.cm_name();
        current_section_start = System.nanoTime();
    }

    public static void end_current_section()
    {
        if (tick_health_requested == 0L || test_type != 1)
        {
            return;
        }
        long end_time = System.nanoTime();
        if (current_tick_start == 0L)
        {
            return;
        }
        if (current_section == null)
        {
            CarpetSettings.LOG.error("finishing section that hasn't started");
            return;
        }
        //CarpetSettings.LOG.error("finishing section "+current_section);
        time_repo.put(current_section,time_repo.get(current_section)+end_time-current_section_start);
        current_section = null;
        current_section_start = 0;
    }

    public static void end_current_entity_section()
    {
        if (tick_health_requested == 0L || test_type != 2)
        {
            return;
        }
        long end_time = System.nanoTime();
        if (current_tick_start == 0L)
        {
            return;
        }
        if (current_section == null)
        {
            CarpetSettings.LOG.error("finishing section that hasn't started");
            return;
        }
        //CarpetSettings.LOG.error("finishing section "+current_section);
        String time_section = "t."+current_section;
        String count_section = "c."+current_section;
        time_repo.put(time_section,time_repo.getOrDefault(time_section,0L)+end_time-current_section_start);
        time_repo.put(count_section,time_repo.getOrDefault(count_section,0L)+1);
        current_section = null;
        current_section_start = 0;
    }

    public static void start_tick_profiling()
    {
        current_tick_start = System.nanoTime();
    }

    public static void end_tick_profiling(MinecraftServer server)
    {
        if (current_tick_start == 0L)
        {
            return;
        }
        time_repo.put("tick",time_repo.get("tick")+System.nanoTime()-current_tick_start);
        tick_health_elapsed --;
        //CarpetSettings.LOG.error("tick count current at "+time_repo.get("tick"));
        if (tick_health_elapsed <= 0)
        {
            finalize_tick_report(server);
        }
    }

    public static void finalize_tick_report(MinecraftServer server)
    {
        if (test_type == 1)
        {
            finalize_tick_report_for_time(server);
        }
        if (test_type == 2)
        {
            finalize_tick_report_for_entities(server);
        }
        cleanup_tick_report();
    }

    public static void cleanup_tick_report()
    {
        time_repo.clear();
        time_repo.put("tick",0L);
        test_type = 0;
        tick_health_elapsed = 0;
        tick_health_requested = 0;
        current_tick_start = 0L;
        current_section_start = 0L;
        current_section = null;

    }

    public static void finalize_tick_report_for_time(MinecraftServer server)
    {
        //print stats
        long total_tick_time = time_repo.get("tick");
        double divider = 1.0D/tick_health_requested/1000000;
        Messenger.print_server_message(server, String.format("Average tick time: %.3fms",divider*total_tick_time));
        long accumulated = 0L;

        accumulated += time_repo.get("Autosave");
        Messenger.print_server_message(server, String.format("Autosave: %.3fms",divider*time_repo.get("Autosave")));

        accumulated += time_repo.get("Network");
        Messenger.print_server_message(server, String.format("Network: %.3fms",divider*time_repo.get("Network")));

        Messenger.print_server_message(server, "Overworld:");

        accumulated += time_repo.get("overworld.entities");
        Messenger.print_server_message(server, String.format(" - Entities: %.3fms",divider*time_repo.get("overworld.entities")));

        accumulated += time_repo.get("overworld.tileentities");
        Messenger.print_server_message(server, String.format(" - Tile Entities: %.3fms",divider*time_repo.get("overworld.tileentities")));

        accumulated += time_repo.get("overworld.blocks");
        Messenger.print_server_message(server, String.format(" - Blocks: %.3fms",divider*time_repo.get("overworld.blocks")));

        accumulated += time_repo.get("overworld.spawning");
        Messenger.print_server_message(server, String.format(" - Spawning: %.3fms",divider*time_repo.get("overworld.spawning")));

        Messenger.print_server_message(server, "Nether:");

        accumulated += time_repo.get("the_nether.entities");
        Messenger.print_server_message(server, String.format(" - Entities: %.3fms",divider*time_repo.get("the_nether.entities")));

        accumulated += time_repo.get("the_nether.tileentities");
        Messenger.print_server_message(server, String.format(" - Tile Entities: %.3fms",divider*time_repo.get("the_nether.tileentities")));

        accumulated += time_repo.get("the_nether.blocks");
        Messenger.print_server_message(server, String.format(" - Blocks: %.3fms",divider*time_repo.get("the_nether.blocks")));

        accumulated += time_repo.get("the_nether.spawning");
        Messenger.print_server_message(server, String.format(" - Spawning: %.3fms",divider*time_repo.get("the_nether.spawning")));

        Messenger.print_server_message(server, "End:");

        accumulated += time_repo.get("the_end.entities");
        Messenger.print_server_message(server, String.format(" - Entities: %.3fms",divider*time_repo.get("the_end.entities")));

        accumulated += time_repo.get("the_end.tileentities");
        Messenger.print_server_message(server, String.format(" - Tile Entities: %.3fms",divider*time_repo.get("the_end.tileentities")));

        accumulated += time_repo.get("the_end.blocks");
        Messenger.print_server_message(server, String.format(" - Blocks: %.3fms",divider*time_repo.get("the_end.blocks")));

        accumulated += time_repo.get("the_end.spawning");
        Messenger.print_server_message(server, String.format(" - Spawning: %.3fms",divider*time_repo.get("the_end.spawning")));

        long rest = total_tick_time-accumulated;

        Messenger.print_server_message(server, String.format("Rest: %.3fms",divider*rest));
    }

    public static void finalize_tick_report_for_entities(MinecraftServer server)
    {
        //print stats
        long total_tick_time = time_repo.get("tick");
        double divider = 1.0D/tick_health_requested/1000000;
        Messenger.print_server_message(server, String.format("Average tick time: %.3fms",divider*total_tick_time));
        time_repo.remove("tick");
        Messenger.print_server_message(server, "Top 10 counts:");
        int total = 0;
        for ( Map.Entry<String, Long> entry : time_repo.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList()) )
        {
            if (entry.getKey().startsWith("t."))
            {
                continue;
            }
            total++;
            if (total > 10)
            {
                continue;
            }
            String[] parts = entry.getKey().split("\\.");
            String dim = parts[1];
            String name = parts[2];
            Messenger.print_server_message(server, String.format(" - %s in %s: %.3f",name, dim, 1.0D*entry.getValue()/tick_health_requested));
        }
        Messenger.print_server_message(server, "Top 10 grossing:");
        total = 0;
        for ( Map.Entry<String, Long> entry : time_repo.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList()) )
        {
            if (entry.getKey().startsWith("c."))
            {
                continue;
            }
            total++;
            if (total > 10)
            {
                continue;
            }
            String[] parts = entry.getKey().split("\\.");
            String dim = parts[1];
            String name = parts[2];
            Messenger.print_server_message(server, String.format(" - %s in %s: %.3fms",name, dim, divider*entry.getValue()));
        }

    }

    public static void prepare_entity_report(int ticks)
    {
        //maybe add so it only spams the sending player, but honestly - all may want to see it
        time_repo.clear();
        time_repo.put("tick",0L);
        test_type = 2;
        tick_health_elapsed = ticks;
        tick_health_requested = ticks;
        current_tick_start = 0L;
        current_section_start = 0L;
        current_section = null;

    }

    public static List<ArrayDeque<Long>> threads = new ArrayList<>();
    public static boolean lastQuestProfiler = false;
    public static boolean lastQuestProfilerStart = false;
    public static boolean endLastQuestProfile = false;

    public static ThreadLocal<ArrayDeque<Long>> profileLastQuest = new ThreadLocal<>();
    public static ThreadLocal<Boolean> mainThread = ThreadLocal.withInitial(() -> false);

    public static void fallingBlockProfile() {
        threads.clear();
        mainThread.set(true);
        initThread();
        lastQuestProfilerStart = true;
        endLastQuestProfile = true;
    }

    public static void endCarpetProfiler(MinecraftServer minecraftServer) {
        if(lastQuestProfiler) {
            while(endLastQuestProfile) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            reportOfLastQuestProfiler(minecraftServer);
            lastQuestProfiler = false;
            lastQuestProfilerStart = false;
        }
    }

    public enum events {
        glass_start,
        glass_end,
        beacon_finish,

        get_begins,
        get_isEmpty,
        get_noCollision,
        get_collision,
        get_collision_finish,

        rehash_start,
        rehash_finish,

        load_chunk,
        chunk_loaded,

        def;

        private static final long ABSURD_NUMBER = -1000000000L;
        public long getConst() { return ordinal() * ABSURD_NUMBER; }
        public static events fromConst(final long x) {return values()[(int) (x / ABSURD_NUMBER)];}
    }
    public final static long GLASS_START = events.glass_start.getConst();
    public final static long GLASS_END = events.glass_end.getConst();
    public final static long BEACON_FINISH = events.beacon_finish.getConst();

    public final static long GET_BEGIN = events.get_begins.getConst();
    public final static long GET_IS_EMPTY = events.get_isEmpty.getConst();
    public final static long GET_COLLISION = events.get_collision.getConst();
    public final static long GET_NO_COLLISION = events.get_noCollision.getConst();
    public final static long GET_COLLISION_FINISHED = events.get_collision_finish.getConst();

    public final static long REHASH_START = events.rehash_start.getConst();
    public final static long REHASH_FINISHED = events.rehash_finish.getConst();

    public final static long LOAD_CHUNK = events.load_chunk.getConst();
    public final static long CHUNK_LOADED = events.chunk_loaded.getConst();



    private static void reportOfLastQuestProfiler(MinecraftServer server) {
        long glass_start = 0,glass_finish = 0, load_chunk = 0, chunk_loaded=0, get_begins = 0, get_finished = 0;
        long rehash_start = 0, rehash_finish = 0, x = 0, z = 0;

        long totalCollisionTime = 0;
        for(ArrayDeque<Long> s : threads) {

            while(!s.isEmpty()){
                long e = s.pop();
                events event = events.def;
                try {
                    event = events.fromConst(e);
                }catch (IndexOutOfBoundsException err){

                }

                switch (event){
                    case glass_start:
                        glass_start = s.pop();
                        Messenger.print_server_message(server, "Glass thread started");
                        break;
                    case glass_end:
                        glass_finish = s.pop();
                        Messenger.print_server_message(server, "Glass thread finished at" + (glass_finish - glass_start));
                        break;
                    case beacon_finish:
                        s.pop();
                        Messenger.print_server_message(server, "Beacon finished");
                        break;
                    case load_chunk:
                        load_chunk = s.pop();
                        x = s.pop();
                        System.out.println(x);
                        z = s.pop();
                        Messenger.print_server_message(server, "Loading chunk" + x + " " + z);
                        break;
                    case chunk_loaded:
                        chunk_loaded = s.pop();
                        Messenger.print_server_message(server, "Loaded took " + (chunk_loaded - load_chunk));
                        break;
                    case get_begins:
                        get_begins = s.pop();
                        Messenger.print_server_message(server, "Get started");
                        break;
                    case get_isEmpty:
                        get_finished = s.pop();
                        totalCollisionTime += (get_finished - get_begins);
                        Messenger.print_server_message(server, "Get was empty, took " + (get_finished - get_begins));
                        break;
                    case get_noCollision:
                        get_finished = s.pop();
                        totalCollisionTime += (get_finished - get_begins);
                        Messenger.print_server_message(server, "Get didn't collided, took " + (get_finished - get_begins));
                        break;
                    case get_collision:
                        get_finished = s.pop();
                        totalCollisionTime += (get_finished - get_begins);
                        Messenger.print_server_message(server, "Get looptiloopty, took " + (get_finished - get_begins));
                        break;
                    case get_collision_finish:
                        get_finished = s.pop();
                        totalCollisionTime += (get_finished - get_begins);
                        Messenger.print_server_message(server, "Get looptiloopty ended, took " + (get_finished - get_begins));
                        break;
                    case rehash_start:
                        rehash_start = s.pop();
                        Messenger.print_server_message(server, "Rehash started");
                        break;
                    case rehash_finish:
                        rehash_finish = s.pop();
                        Messenger.print_server_message(server, "Rehash finished " + (rehash_finish - rehash_start));
                        break;

                    default:
                        Messenger.print_server_message(server, "unrecognized error" + e);
                }
            }
        }

        ArrayList<Pair<String, Long>> collisions = new ArrayList<>();
        collisions.add(new Pair<String, Long>( "Glass Start", glass_start));
        collisions.add(new Pair<String, Long>( "Rehash Finish", rehash_finish));
        collisions.add(new Pair<String, Long>( "Glass Finish", glass_finish));

        collisions
                .stream()
                .sorted(Comparator.comparing(Pair::getValue))
                .forEach( pair -> Messenger.print_server_message(server, pair.getKey()));


        long totalGlass = (glass_finish - glass_start);

        Messenger.print_server_message(server, "Total time in glass thread: " + totalGlass);
        Messenger.print_server_message(server, "Total time in getBlockState: " + totalCollisionTime);
        Messenger.print_server_message(server, "Time Spend ratio: " + String.format("%.2f", (totalCollisionTime / totalGlass) * 100));
        Messenger.print_server_message(server, "Total time in rehash: " + (rehash_finish - rehash_start));
        Messenger.print_server_message(server, "Chunk " + x + " " + z + " load time: " + (chunk_loaded - load_chunk));
    }

    public static void initThread() {
        ArrayDeque<Long> deque = new ArrayDeque<>();
        threads.add(deque);
        profileLastQuest.set(deque);
    }
}
