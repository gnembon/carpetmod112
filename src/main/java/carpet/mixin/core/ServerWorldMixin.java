package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import net.minecraft.class_1268;
import net.minecraft.class_6380;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = ServerWorld.class, priority = 1001)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(class_1268 levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Shadow protected abstract void sendBlockActions();

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;method_26212(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"))
    private int findChunksForSpawning(SpawnHelper worldEntitySpawner, ServerWorld worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        return TickSpeed.process_entities ? worldEntitySpawner.method_26212(worldServerIn, spawnHostileMobs, spawnPeacefulMobs, spawnOnSetTickRate) : 0;
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setTime(J)V"))
    private void setWorldTotalTime(LevelProperties worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setTime(time);
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setTimeOfDay(J)V"))
    private void setWorldTime(LevelProperties worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setTimeOfDay(time);
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_26051(Z)Z"))
    private boolean tickUpdates(ServerWorld worldServer, boolean runAllPending) {
        return TickSpeed.process_entities && worldServer.method_26051(runAllPending);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33590()V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelIfNotProcessEntities(CallbackInfo ci) {
        if (!TickSpeed.process_entities) {
            this.profiler.pop();
            this.sendBlockActions();
            ci.cancel();
        }
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33585()Ljava/util/Iterator;", ordinal = 1))
    private Iterator<WorldChunk> getChunkIterator(class_6380 map) {
        Iterator<WorldChunk> iterator = map.method_33585();
        if (!TickSpeed.process_entities) {
            while (iterator.hasNext()) {
                this.profiler.push("getChunk");
                WorldChunk chunk = iterator.next();
                this.profiler.swap("checkNextLight");
                chunk.method_27420();
                this.profiler.swap("tickChunk");
                chunk.method_27391(false);
                this.profiler.pop();
            }
            // now the iterator is done and the vanilla loop won't run
            // this acts like a `continue` after chunk.onTick(false)
        }
        return iterator;
    }
}
