package carpet.mixin.entityDuplicationFix;

import carpet.CarpetSettings;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ColumnPos;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements SkinOverlayOwner {
    @Shadow @Final private final Map<ColumnPos, CompoundTag> field_25416 = new HashMap<>();
    @Shadow private boolean chunkHolderListDirty;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private File field_25418;

    @Shadow protected abstract void method_27474(ColumnPos pos, CompoundTag compound) throws IOException;

    private final Map<ColumnPos, CompoundTag> chunksInWrite = new HashMap<>();
    // Insert new chunk into pending queue, replacing any older one at the same position
    synchronized private void queueChunkToRemove(ColumnPos pos, CompoundTag data) {
        field_25416.put(pos, data);
    }

    // Fetch another chunk to save to disk and atomically move it into
    // the queue of chunk(s) being written.
    synchronized private Map.Entry<ColumnPos, CompoundTag> fetchChunkToWrite() {
        if (field_25416.isEmpty()) return null;
        Iterator<Map.Entry<ColumnPos, CompoundTag>> iter =
                field_25416.entrySet().iterator();
        Map.Entry<ColumnPos, CompoundTag> entry = iter.next();
        iter.remove();
        chunksInWrite.put(entry.getKey(), entry.getValue());
        return entry;
    }

    // Once the write for a chunk is completely committed to disk,
    // this method discards it
    synchronized private void retireChunkToWrite(ColumnPos pos, CompoundTag data) {
        chunksInWrite.remove(pos);
    }

    // Check these data structures for a chunk being reloaded
    synchronized private CompoundTag reloadChunkFromRemoveQueues(ColumnPos pos) {
        CompoundTag data = field_25416.get(pos);
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
        "method_27476",
        "method_27475"
    }, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object get(Map<ColumnPos, CompoundTag> map, Object key) {
        return reloadChunkFromRemoveQueues((ColumnPos) key);
    }

    @Redirect(method = "method_27464", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean isInWrite(Set<ColumnPos> set, Object o) {
        if (CarpetSettings.entityDuplicationFix) return false;
        //noinspection SuspiciousMethodCalls
        return chunksInWrite.containsKey(o);
    }

    @Redirect(method = "method_27464", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object queueRemove(Map<ColumnPos, CompoundTag> map, Object key, Object value) {
        queueChunkToRemove((ColumnPos) key, (CompoundTag) value);
        return null;
    }

    @Overwrite
    public boolean shouldRenderOverlay() {
        Map.Entry<ColumnPos, CompoundTag> entry = fetchChunkToWrite();
        if (entry == null) {
            if (this.chunkHolderListDirty) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", this.field_25418.getName());
            }

            return false;
        }

        ColumnPos pos = entry.getKey();
        CompoundTag tag = entry.getValue();
        try {
            this.method_27474(pos, tag);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }

        retireChunkToWrite(pos, tag);
        return true;
    }
}
