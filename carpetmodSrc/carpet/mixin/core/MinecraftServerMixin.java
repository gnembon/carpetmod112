package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.ScoreboardDelta;
import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ReportedException;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int tickCounter;
    @Shadow @Final private ServerStatusResponse statusResponse;
    @Shadow private boolean serverRunning;
    @Shadow private long currentTime;
    @Shadow private boolean serverIsRunning;
    @Shadow private long timeOfLastWarning;
    @Shadow public WorldServer[] worlds;
    @Shadow private boolean serverStopped;

    @Shadow protected abstract void stopServer();
    @Shadow public abstract void systemExitNow();
    @Shadow protected abstract void finalTick(CrashReport report);
    @Shadow public abstract CrashReport addServerInfoToCrashReport(CrashReport report);
    @Shadow public abstract File getDataDirectory();
    @Shadow protected abstract void tick();
    @Shadow public abstract void applyServerIconToResponse(ServerStatusResponse response);
    @Shadow public abstract boolean init() throws IOException;
    @Shadow public static long getCurrentTimeMillis() { throw new AbstractMethodError(); }

    @Shadow private String motd;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn, CallbackInfo ci) {
        CarpetServer.init((MinecraftServer) (Object) this);
    }

    @Inject(method = "loadAllWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initialWorldChunkLoad()V"))
    private void onLoadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions, CallbackInfo ci) {
        CarpetServer.onLoadAllWorlds((MinecraftServer) (Object) this);
    }

    @Inject(method = "saveAllWorlds", at = @At("RETURN"))
    private void onWorldsSaved(boolean isSilent, CallbackInfo ci) {
        CarpetServer.onWorldsSaved((MinecraftServer) (Object) this);
    }

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void onMain(String[] args, CallbackInfo ci) {
        System.out.println(Arrays.toString(args));
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;tickCounter:I", ordinal = 0, shift = At.Shift.AFTER))
    private void startTick(CallbackInfo ci) {
        CarpetServer.tick((MinecraftServer) (Object) this);
        if (CarpetProfiler.tick_health_requested != 0) {
            CarpetProfiler.start_tick_profiling();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void endTick(CallbackInfo ci) {
        // ChunkLogger - 0x-CARPET
        if(CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.sendAll();
        }

        if (CarpetProfiler.tick_health_requested != 0L)
        {
            CarpetProfiler.end_tick_profiling((MinecraftServer) (Object) this);
        }

        if(CarpetSettings.scoreboardDelta > 0 && this.tickCounter % 20 == 0){
            ScoreboardDelta.update();
        }
    }

    /**
     * @author gnembon
     */
    @Overwrite
    public String getServerModName() {
        return "carpetmod";
    }

    /**
     * @author gnembon, X-com, skyrising, 0x53ee71ebe11e
     */
    @Overwrite
    public void run() {
        try {
            if (this.init()) {
                this.currentTime = getCurrentTimeMillis();
                long msGoal = 0L;
                String motd = "_".equals(CarpetSettings.customMOTD) ? this.motd : CarpetSettings.customMOTD;
                this.statusResponse.setServerDescription(new TextComponentString(motd));
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning) {
                    /* carpet mod commandTick */
                    //todo check if this check is necessary
                    if (TickSpeed.time_warp_start_time != 0) {
                        if (TickSpeed.continueWarp()) {
                            this.tick();
                            this.currentTime = getCurrentTimeMillis();
                            this.serverIsRunning = true;
                        }
                        continue;
                    }
                    /* end */
                    long now = getCurrentTimeMillis();
                    long timeDelta = now - this.currentTime;

                    if (timeDelta > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L) {
                        timeDelta = 2000L;
                        this.timeOfLastWarning = this.currentTime;
                    }

                    if (timeDelta < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        timeDelta = 0L;
                    }

                    msGoal += timeDelta;
                    this.currentTime = now;
                    boolean falling_behind = false;

                    if (this.worlds[0].areAllPlayersAsleep()) {
                        this.tick();
                        msGoal = 0L;
                    } else {
                        boolean keeping_up = false;
                        while (msGoal > TickSpeed.mspt) /* carpet mod 50L */ {
                            msGoal -= TickSpeed.mspt; /* carpet mod 50L */
                            if (CarpetSettings.watchdogFix && keeping_up) {
                                this.currentTime = getCurrentTimeMillis();
                                this.serverIsRunning = true;
                                falling_behind = true;
                            }
                            this.tick();
                            keeping_up = true;
                            if (CarpetSettings.disableVanillaTickWarp) {
                                msGoal = getCurrentTimeMillis() - now;
                                break;
                            }
                        }
                    }

                    if (falling_behind) {
                        Thread.sleep(1L); /* carpet mod 50L */
                    } else {
                        Thread.sleep(Math.max(1L, TickSpeed.mspt - msGoal)); /* carpet mod 50L */
                    }
                    this.serverIsRunning = true;
                }
            } else {
                this.finalTick(null);
            }
        } catch (Throwable throwable1) {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;
            if (throwable1 instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1)) {
                LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.finalTick(crashreport);
        } finally {
            try {
                this.serverStopped = true;
                this.stopServer();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                this.systemExitNow();
            }
        }
    }
}
