package carpet.mixin.core;

import carpet.utils.extensions.RepopulatableChunk;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public class ChunkMixin implements RepopulatableChunk {
    @Shadow private boolean isTerrainPopulated;

    @Override
    public void setUnpopulated() {
        this.isTerrainPopulated = false;
    }
}
