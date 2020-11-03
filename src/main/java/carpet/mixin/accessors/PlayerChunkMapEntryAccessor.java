package carpet.mixin.accessors;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerChunkMapEntry.class)
public interface PlayerChunkMapEntryAccessor {
    @Accessor List<EntityPlayerMP> getPlayers();
    @Accessor void setChunk(Chunk chunk);
    @Accessor void setSentToPlayers(boolean sent);
}
