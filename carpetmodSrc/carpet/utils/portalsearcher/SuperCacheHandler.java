package carpet.utils.portalsearcher;

import carpet.CarpetSettings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SuperCacheHandler {
    private Map<ChunkPos, Set<BlockPos>> chunkPortalMapping;

    private static SuperCacheHandler HANDLER_OVERWORLD = new SuperCacheHandler();
    private static SuperCacheHandler HANDLER_NETHER = new SuperCacheHandler();

    private SuperCacheHandler() {
        chunkPortalMapping = Maps.newHashMap();
    }

    public static SuperCacheHandler getHandlerOverworld() {
        return HANDLER_OVERWORLD;
    }

    public static SuperCacheHandler getHandlerNether() {
        return HANDLER_NETHER;
    }

    public boolean addPortal(BlockPos portalPos) {
        if (!checkOption()) {
            return false;
        }
        ChunkPos cPos = new ChunkPos(portalPos);
        if (!chunkPortalMapping.containsKey(cPos)) {
            return false;
        }
        if (chunkPortalMapping.get(cPos) == null) {
            chunkPortalMapping.put(cPos, Sets.newHashSet());
        }
        chunkPortalMapping.get(cPos).add(new BlockPos(portalPos));
        return true;
    }

    public boolean markChunk(ChunkPos chunkPos) {
        if (!checkOption()) {
            return false;
        }
        if (!chunkPortalMapping.containsKey(chunkPos)) {
            chunkPortalMapping.put(chunkPos, null);
            return true;
        }
        return false;
    }

    public boolean removePortal(BlockPos portalPos) {
        if (!checkOption()) {
            return false;
        }
        ChunkPos cPos = new ChunkPos(portalPos);
        if (!chunkPortalMapping.containsKey(cPos)) {
            return false;
        }
        return chunkPortalMapping.get(cPos).remove(new BlockPos(portalPos));
    }

    public boolean isMarked(ChunkPos chunkPos) {
        return chunkPortalMapping.containsKey(chunkPos);
    }

    public Iterable<BlockPos> getChunkPortalIterable(ChunkPos chunkPos) {
        if (!isMarked(chunkPos)) {
            return null;
        }
        Iterable iterable = chunkPortalMapping.get(chunkPos);
        return iterable == null ? Collections::emptyIterator : iterable;
    }

    public void clear() {
        chunkPortalMapping.clear();
    }

    private boolean checkOption() {
        if (!CarpetSettings.portalSuperCache) {
            this.clear();
            return false;
        } else {
            return true;
        }
    }
}
