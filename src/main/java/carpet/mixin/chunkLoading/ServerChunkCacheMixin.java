package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.ChunkLoading;
import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_4615;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
    private boolean fakePermaloaderProtected;

    @Shadow @Final private ServerWorld world;
    @Shadow @Final private Long2ObjectMap<Chunk> field_31697;

    @Shadow protected abstract void method_33453(Chunk chunkIn);
    @Shadow @Nullable public abstract Chunk method_33452(int x, int z);

    @Redirect(method = "method_33448", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;method_27511(II)Z"))
    private boolean canDrop(Dimension dimension, int x, int z) {
        if (CarpetSettings.tickingAreas && TickingArea.isTickingChunk(world, x, z)) return false;
        if (CarpetSettings.disableSpawnChunks) return true;
        return dimension.method_27511(x, z);
    }

    @Inject(method = "method_33451", at = @At("HEAD"))
    private void onQueueAll(CallbackInfo ci) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Inject(method = "method_33450", at = @At("RETURN"))
    private void onSaveChunks(boolean all, CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", remap = false))
    private boolean droppedChunksIsEmpty(Set<Long> droppedChunks) {
        return droppedChunks.isEmpty() || fakePermaloaderProtected;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/Chunk;field_25367:Z"))
    private boolean isUnloadQueued(Chunk chunk) {
        if (chunk.field_25367) return true;
        if (CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            //noinspection ConstantConditions
            if (CarpetSettings.whereToChunkSavestate == CarpetSettings.WhereToChunkSavestate.everywhere
                    || world.getPlayers(Entity.class, player -> player.chunkX == chunk.x && player.chunkZ == chunk.z).isEmpty()) {
                // Getting the chunk size is incredibly inefficient, but it's better than unloading and reloading the chunk
                if ((ChunkLoading.getSavedChunkSize(chunk) + 5) / 4096 + 1 >= 256) {
                    chunk.method_27398();
                    //this.saveChunkData(chunk); no point saving the chunk data, we know that won't work
                    this.method_33453(chunk);
                    this.field_31697.remove(ColumnPos.method_25891(chunk.x, chunk.z));
                    //++i; don't break stuff
                    Chunk newChunk = this.method_33452(chunk.x, chunk.z);
                    if (newChunk != null)
                        newChunk.method_27391(true);
                    class_4615 pcmEntry = ((ServerWorldAccessor) world).getPlayerChunkMap().method_33587(chunk.x, chunk.z);
                    if (pcmEntry != null) {
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setChunk(newChunk);
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setSentToPlayers(false);
                        pcmEntry.method_33568();
                    }
                }
            }
        }
        return false;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5318;method_27478()V"))
    private void resetFakePermaloader(CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = false;
    }
}
