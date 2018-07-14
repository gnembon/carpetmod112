package carpet.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class DeathTracker
{
    //public static void entityIsBorn(World worldIn, EntityLivingBase entityLivingBase)
    //{
    //    ; //if (!(entityLivingBase instanceof EntityPlayer)) Messenger.print_server_message(worldIn.getMinecraftServer(),entityLivingBase.getDisplayName());
    //}

    public static class DeathCertificate
    {
        public BlockPos location;
        public long lifetime;
        public String cause;
    }
    public static boolean active = false;
    private static long start_tick;
    private static EvictingQueue<DeathCertificate,Integer> lastDeaths = new EvictingQueue<>();
    private static EvictingQueue<DeathCertificate,Integer> longestDeaths = new EvictingQueue<>();
    private static EvictingQueue<DeathCertificate,Integer> shortestDeaths = new EvictingQueue<>();

    private static void clean_results()
    {
        lastDeaths.clear();
        longestDeaths.clear();
        shortestDeaths.clear();

    }
    public static void start(MinecraftServer server)
    {
        clean_results();
        start_tick = server.getTickCounter();
        active = true;
    }
    public static List<ITextComponent> query_summary(MinecraftServer server)
    {
        return new ArrayList<ITextComponent>();
    }

    public static List<ITextComponent> stop(MinecraftServer server)
    {
        List<ITextComponent> res = query_summary(server);
        clean_results();
        active = false;
        start_tick = 0L;
        return res;
    }
    public static List<ITextComponent> query_moblist(MinecraftServer server)
    {
        return new ArrayList<ITextComponent>();
    }
}
