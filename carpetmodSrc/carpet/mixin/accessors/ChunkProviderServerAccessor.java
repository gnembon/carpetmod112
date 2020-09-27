package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public interface ChunkProviderServerAccessor {
    @Accessor Set<Long> getDroppedChunks();
    @Accessor IChunkGenerator getChunkGenerator();
    @Accessor IChunkLoader getChunkLoader();
    @Accessor("loadedChunks") Long2ObjectMap<Chunk> getLoadedChunksMap();
}
