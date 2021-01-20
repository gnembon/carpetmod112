package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_5318;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerAccessor {
    @Accessor("field_31694") Set<Long> getDroppedChunks();
    @Accessor("field_31695") ChunkManager getChunkGenerator();
    @Accessor("field_31696") class_5318 getChunkLoader();
    @Accessor("field_31697") Long2ObjectMap<WorldChunk> getLoadedChunksMap();
}
