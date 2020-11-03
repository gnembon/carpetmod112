package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.WorldServerAccessor;
import carpet.utils.ChunkLoading;
import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
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

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin {
    private boolean fakePermaloaderProtected;

    @Shadow @Final private WorldServer world;
    @Shadow @Final private Long2ObjectMap<Chunk> loadedChunks;

    @Shadow protected abstract void saveChunkExtraData(Chunk chunkIn);
    @Shadow @Nullable public abstract Chunk loadChunk(int x, int z);

    @Redirect(method = "queueUnload", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;canDropChunk(II)Z"))
    private boolean canDrop(WorldProvider worldProvider, int x, int z) {
        if (CarpetSettings.tickingAreas && TickingArea.isTickingChunk(world, x, z)) return false;
        if (CarpetSettings.disableSpawnChunks) return true;
        return worldProvider.canDropChunk(x, z);
    }

    @Inject(method = "queueUnloadAll", at = @At("HEAD"))
    private void onQueueAll(CallbackInfo ci) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Inject(method = "saveChunks", at = @At("RETURN"))
    private void onSaveChunks(boolean all, CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", remap = false))
    private boolean droppedChunksIsEmpty(Set<Long> droppedChunks) {
        return droppedChunks.isEmpty() || fakePermaloaderProtected;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/Chunk;unloadQueued:Z"))
    private boolean isUnloadQueued(Chunk chunk) {
        if (chunk.unloadQueued) return true;
        if (CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            //noinspection ConstantConditions
            if (CarpetSettings.whereToChunkSavestate == CarpetSettings.WhereToChunkSavestate.everywhere
                    || world.getPlayers(Entity.class, player -> player.chunkCoordX == chunk.x && player.chunkCoordZ == chunk.z).isEmpty()) {
                // Getting the chunk size is incredibly inefficient, but it's better than unloading and reloading the chunk
                if ((ChunkLoading.getSavedChunkSize(chunk) + 5) / 4096 + 1 >= 256) {
                    chunk.onUnload();
                    //this.saveChunkData(chunk); no point saving the chunk data, we know that won't work
                    this.saveChunkExtraData(chunk);
                    this.loadedChunks.remove(ChunkPos.asLong(chunk.x, chunk.z));
                    //++i; don't break stuff
                    Chunk newChunk = this.loadChunk(chunk.x, chunk.z);
                    if (newChunk != null)
                        newChunk.onTick(true);
                    PlayerChunkMapEntry pcmEntry = ((WorldServerAccessor) world).getPlayerChunkMap().getEntry(chunk.x, chunk.z);
                    if (pcmEntry != null) {
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setChunk(newChunk);
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setSentToPlayers(false);
                        pcmEntry.sendToPlayers();
                    }
                }
            }
        }
        return false;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;chunkTick()V"))
    private void resetFakePermaloader(CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = false;
    }
}
