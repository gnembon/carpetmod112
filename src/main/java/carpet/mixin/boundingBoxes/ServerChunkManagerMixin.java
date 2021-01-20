package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.ChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin implements BoundingBoxProvider {
    @Shadow @Final private ChunkManager field_31695;

    public ListTag getBoundingBoxes(Entity entity) {
        if (field_31695 instanceof BoundingBoxProvider) return ((BoundingBoxProvider) field_31695).getBoundingBoxes(entity);
        return new ListTag();
    }
}
