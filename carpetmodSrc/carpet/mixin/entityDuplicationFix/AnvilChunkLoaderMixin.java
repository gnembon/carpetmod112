package carpet.mixin.entityDuplicationFix;

import carpet.CarpetSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(AnvilChunkLoader.class)
public abstract class AnvilChunkLoaderMixin {
    @Shadow private @Final Map<ChunkPos, NBTTagCompound> chunksToSave = new HashMap<>();
    @Shadow private boolean flushing;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private File chunkSaveLocation;

    @Shadow protected abstract void writeChunkData(ChunkPos pos, NBTTagCompound compound) throws IOException;

    private final Map<ChunkPos, NBTTagCompound> chunksInWrite = new HashMap<>();
    // Insert new chunk into pending queue, replacing any older one at the same position
    synchronized private void queueChunkToRemove(ChunkPos pos, NBTTagCompound data) {
        chunksToSave.put(pos, data);
    }

    // Fetch another chunk to save to disk and atomically move it into
    // the queue of chunk(s) being written.
    synchronized private Map.Entry<ChunkPos, NBTTagCompound> fetchChunkToWrite() {
        if (chunksToSave.isEmpty()) return null;
        Iterator<Map.Entry<ChunkPos, NBTTagCompound>> iter =
                chunksToSave.entrySet().iterator();
        Map.Entry<ChunkPos, NBTTagCompound> entry = iter.next();
        iter.remove();
        chunksInWrite.put(entry.getKey(), entry.getValue());
        return entry;
    }

    // Once the write for a chunk is completely committed to disk,
    // this method discards it
    synchronized private void retireChunkToWrite(ChunkPos pos, NBTTagCompound data) {
        chunksInWrite.remove(pos);
    }

    // Check these data structures for a chunk being reloaded
    synchronized private NBTTagCompound reloadChunkFromRemoveQueues(ChunkPos pos) {
        NBTTagCompound data = chunksToSave.get(pos);
        if (data != null) return data;
        return (CarpetSettings.entityDuplicationFix)?chunksInWrite.get(pos):data;
    }

    // Check if chunk exists at all in any pending save state
    //synchronized private boolean chunkExistInRemoveQueues(ChunkPos pos)
    //{
    //    return chunksToRemove.containsKey(pos) || chunksInWrite.containsKey(pos);
    //}

    /* --- end of new code for MC-119971 --- */

    @Redirect(method = {
        "loadChunk",
        "isChunkGeneratedAt"
    }, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object get(Map<ChunkPos, NBTTagCompound> map, Object key) {
        return reloadChunkFromRemoveQueues((ChunkPos) key);
    }

    @Redirect(method = "addChunkToPending", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean isInWrite(Set<ChunkPos> set, Object o) {
        if (CarpetSettings.entityDuplicationFix) return false;
        //noinspection SuspiciousMethodCalls
        return chunksInWrite.containsKey(o);
    }

    @Redirect(method = "addChunkToPending", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object queueRemove(Map<ChunkPos, NBTTagCompound> map, Object key, Object value) {
        queueChunkToRemove((ChunkPos) key, (NBTTagCompound) value);
        return null;
    }

    @Overwrite
    public boolean writeNextIO() {
        Map.Entry<ChunkPos, NBTTagCompound> entry = fetchChunkToWrite();
        if (entry == null) {
            if (this.flushing) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", this.chunkSaveLocation.getName());
            }

            return false;
        }

        ChunkPos pos = entry.getKey();
        NBTTagCompound tag = entry.getValue();
        try {
            this.writeChunkData(pos, tag);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }

        retireChunkToWrite(pos, tag);
        return true;
    }
}
