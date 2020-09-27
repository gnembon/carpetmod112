package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = WorldServer.class, priority = 1001)
public abstract class WorldServerMixin extends World {
    @Shadow protected abstract void sendQueuedBlockEvents();

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldEntitySpawner;findChunksForSpawning(Lnet/minecraft/world/WorldServer;ZZZ)I"))
    private int findChunksForSpawning(WorldEntitySpawner worldEntitySpawner, WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        return TickSpeed.process_entities ? worldEntitySpawner.findChunksForSpawning(worldServerIn, spawnHostileMobs, spawnPeacefulMobs, spawnOnSetTickRate) : 0;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setWorldTotalTime(J)V"))
    private void setWorldTotalTime(WorldInfo worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setWorldTotalTime(time);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setWorldTime(J)V"))
    private void setWorldTime(WorldInfo worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setWorldTime(time);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tickUpdates(Z)Z"))
    private boolean tickUpdates(WorldServer worldServer, boolean runAllPending) {
        return TickSpeed.process_entities && worldServer.tickUpdates(runAllPending);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;tick()V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelIfNotProcessEntities(CallbackInfo ci) {
        if (!TickSpeed.process_entities) {
            this.profiler.endSection();
            this.sendQueuedBlockEvents();
            ci.cancel();
        }
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;getChunkIterator()Ljava/util/Iterator;"))
    private Iterator<Chunk> getChunkIterator(PlayerChunkMap map) {
        Iterator<Chunk> iterator = map.getChunkIterator();
        if (!TickSpeed.process_entities) {
            while (iterator.hasNext()) {
                this.profiler.startSection("getChunk");
                Chunk chunk = iterator.next();
                this.profiler.endStartSection("checkNextLight");
                chunk.enqueueRelightChecks();
                this.profiler.endStartSection("tickChunk");
                chunk.onTick(false);
                this.profiler.endSection();
            }
            // now the iterator is done and the vanilla loop won't run
            // this act's like a `continue` after chunk.onTick(false)
        }
        return iterator;
    }
}
