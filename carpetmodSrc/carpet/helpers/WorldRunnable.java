package carpet.helpers;

import net.minecraft.crash.CrashReport;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;

import java.util.concurrent.*;

public class WorldRunnable implements Runnable{

    public final WorldServer worldserver;
    private final boolean ignoreAllowNether;
    private final Phaser phaser;
    private final long[] tickTimeArray = new long[100];
    private int tickCounter = 0;

    public WorldRunnable(WorldServer worldServer, Phaser phaser){
        this.phaser = phaser;
        this.worldserver = worldServer;
        this.ignoreAllowNether = this.worldserver == this.worldserver.getMinecraftServer().getWorld(0);
    }

    public void run() {
        MinecraftServer minecraftServer = worldserver.getMinecraftServer();
        phaser.register();
        while(!phaser.isTerminated()) {

            phaser.arriveAndAwaitAdvance(); //Start of the tick barrier

//            long time = minecraftServer.getCurrentTimeMillis();
            long i = System.nanoTime();

            if (this.ignoreAllowNether || minecraftServer.getAllowNether())

                minecraftServer.profiler.startSection(worldserver.getWorldInfo().getWorldName());

            if (minecraftServer.getTickCounter() % 20 == 0)
            {
                //this.theProfiler.startSection("timeSync");
                minecraftServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimensionType().getId());
                //this.theProfiler.endSection();
            }

            //this.theProfiler.startSection("tick");

            try
            {
                worldserver.tick();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                worldserver.addWorldInfoToCrashReport(crashreport);
                throw new ReportedException(crashreport);
            }

            try
            {
                worldserver.updateEntities();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                worldserver.addWorldInfoToCrashReport(crashreport1);
                throw new ReportedException(crashreport1);
            }

            //this.theProfiler.endSection();
            //this.theProfiler.startSection("tracker");
            worldserver.getEntityTracker().tick(); //Probably already thread-safe
            //this.theProfiler.endSection();


            //this.theProfiler.endSection();

            tickCounter++;
            this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
//            time = MinecraftServer.getCurrentTimeMillis() - time;
            //We don't want to count waiting for other Threads as tick time

            phaser.arriveAndAwaitAdvance(); //Teleport barrier


//            long time1 = MinecraftServer.getCurrentTimeMillis();

            try
            {
                worldserver.placeTeleportedEntities();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                worldserver.addWorldInfoToCrashReport(crashreport1);
                throw new ReportedException(crashreport1);
            }
//            time1 = MinecraftServer.getCurrentTimeMillis() - time1;

//            minecraftServer.logInfo(worldserver.provider.getDimensionType().getName() + " tick finished in ms: " + (String.valueOf(time + time1)));
//
            phaser.arriveAndAwaitAdvance(); //End of the tick barrier

        }
        minecraftServer.logInfo("Thread ended as server is shutting down");
    }

    public long[] getTickTimeArray() {
        return tickTimeArray;
    }
}
