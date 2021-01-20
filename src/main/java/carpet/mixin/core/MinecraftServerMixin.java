package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.ScoreboardDelta;
import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.LevelGeneratorType;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int ticks;
    @Shadow @Final private ServerMetadata metadata;
    @Shadow private boolean running;
    @Shadow private long timeReference;
    @Shadow private boolean loading;
    @Shadow private long field_4557;
    @Shadow public ServerWorld[] worlds;
    @Shadow private boolean stopped;

    @Shadow public abstract void shutdown();
    @Shadow public abstract void exit();
    @Shadow public abstract void setCrashReport(CrashReport report);
    @Shadow public abstract CrashReport populateCrashReport(CrashReport report);
    @Shadow public abstract File getRunDirectory();
    @Shadow public abstract void tick();
    @Shadow public abstract void setFavicon(ServerMetadata response);
    @Shadow public abstract boolean setupServer() throws IOException;
    @Shadow public static long getMeasuringTimeMs() { throw new AbstractMethodError(); }

    @Shadow private String motd;

    @Inject(method = "createWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion()V"))
    private void onLoadAllWorlds(String saveName, String worldNameIn, long seed, LevelGeneratorType type, String generatorOptions, CallbackInfo ci) {
        CarpetServer.onLoadAllWorlds((MinecraftServer) (Object) this);
    }

    @Inject(method = "createWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion()V", shift = At.Shift.AFTER))
    private void loadCarpetBots(String saveName, String worldNameIn, long seed, LevelGeneratorType type, String generatorOptions, CallbackInfo ci) {
        CarpetServer.loadBots((MinecraftServer) (Object) this);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void onWorldsSaved(boolean isSilent, CallbackInfo ci) {
        CarpetServer.onWorldsSaved((MinecraftServer) (Object) this);
    }

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void onMain(String[] args, CallbackInfo ci) {
        System.out.println(Arrays.toString(args));
    }

    @Inject(method = "tickWorlds", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;ticks:I", ordinal = 0, shift = At.Shift.AFTER))
    private void startTick(CallbackInfo ci) {
        CarpetServer.tick((MinecraftServer) (Object) this);
        if (CarpetProfiler.tick_health_requested != 0) {
            CarpetProfiler.start_tick_profiling();
        }
    }

    @Inject(method = "tickWorlds", at = @At("RETURN"))
    private void endTick(CallbackInfo ci) {
        // ChunkLogger - 0x-CARPET
        if(CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.sendAll();
        }

        if (CarpetProfiler.tick_health_requested != 0L)
        {
            CarpetProfiler.end_tick_profiling((MinecraftServer) (Object) this);
        }

        if(CarpetSettings.scoreboardDelta > 0 && this.ticks % 20 == 0){
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
            if (this.setupServer()) {
                this.timeReference = getMeasuringTimeMs();
                long msGoal = 0L;
                String motd = "_".equals(CarpetSettings.customMOTD) ? this.motd : CarpetSettings.customMOTD;
                this.metadata.setDescription(new LiteralText(motd));
                this.metadata.setVersion(new ServerMetadata.Version("1.12.2", 340));
                this.setFavicon(this.metadata);

                while (this.running) {
                    /* carpet mod commandTick */
                    //todo check if this check is necessary
                    if (TickSpeed.time_warp_start_time != 0) {
                        if (TickSpeed.continueWarp()) {
                            this.tick();
                            this.timeReference = getMeasuringTimeMs();
                            this.loading = true;
                        }
                        continue;
                    }
                    /* end */
                    long now = getMeasuringTimeMs();
                    long timeDelta = now - this.timeReference;

                    if (timeDelta > 2000L && this.timeReference - this.field_4557 >= 15000L) {
                        timeDelta = 2000L;
                        this.field_4557 = this.timeReference;
                    }

                    if (timeDelta < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        timeDelta = 0L;
                    }

                    msGoal += timeDelta;
                    this.timeReference = now;
                    boolean falling_behind = false;

                    if (this.worlds[0].method_33479()) {
                        this.tick();
                        msGoal = 0L;
                    } else {
                        boolean keeping_up = false;
                        while (msGoal > TickSpeed.mspt) /* carpet mod 50L */ {
                            msGoal -= TickSpeed.mspt; /* carpet mod 50L */
                            if (CarpetSettings.watchdogFix && keeping_up) {
                                this.timeReference = getMeasuringTimeMs();
                                this.loading = true;
                                falling_behind = true;
                            }
                            this.tick();
                            keeping_up = true;
                            if (CarpetSettings.disableVanillaTickWarp) {
                                msGoal = getMeasuringTimeMs() - now;
                                break;
                            }
                        }
                    }

                    if (falling_behind) {
                        Thread.sleep(1L); /* carpet mod 50L */
                    } else {
                        Thread.sleep(Math.max(1L, TickSpeed.mspt - msGoal)); /* carpet mod 50L */
                    }
                    this.loading = true;
                }
            } else {
                this.setCrashReport(null);
            }
        } catch (Throwable throwable1) {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;
            if (throwable1 instanceof CrashException) {
                crashreport = this.populateCrashReport(((CrashException) throwable1).getReport());
            } else {
                crashreport = this.populateCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getRunDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");

            if (crashreport.writeToFile(file1)) {
                LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.setCrashReport(crashreport);
        } finally {
            try {
                this.stopped = true;
                this.shutdown();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                this.exit();
            }
        }
    }
}
