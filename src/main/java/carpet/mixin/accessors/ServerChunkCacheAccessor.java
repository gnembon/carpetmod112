package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_5318;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheAccessor {
    @Accessor("field_31694") Set<Long> getDroppedChunks();
    @Accessor("field_31695") ChunkGenerator getChunkGenerator();
    @Accessor("field_31696") class_5318 getChunkLoader();
    @Accessor("field_31697") Long2ObjectMap<Chunk> getLoadedChunksMap();
}
