package carpet.mixin.core;

import carpet.utils.extensions.RepopulatableChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public class WorldChunkMixin implements RepopulatableChunk {
    @Shadow private boolean field_25379;

    @Override
    public void setUnpopulated() {
        this.field_25379 = false;
    }
}
