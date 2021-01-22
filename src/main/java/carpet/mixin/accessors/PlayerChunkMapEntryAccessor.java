package carpet.mixin.accessors;

import net.minecraft.class_4615;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(class_4615.class)
public interface PlayerChunkMapEntryAccessor {
    @Accessor("field_31793") List<ServerPlayerEntity> getPlayers();
    @Accessor("field_31796") void setChunk(Chunk chunk);
    @Accessor("field_31800") void setSentToPlayers(boolean sent);
}
