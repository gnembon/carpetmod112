package carpet.mixin.accessors;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerChunkMap.class)
public interface PlayerChunkMapAccessor {
    @Accessor List<PlayerChunkMapEntry> getEntries();
    @Accessor List<PlayerChunkMapEntry> getEntriesWithoutChunks();
}
