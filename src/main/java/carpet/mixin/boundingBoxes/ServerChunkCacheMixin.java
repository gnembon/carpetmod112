package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.world.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin implements BoundingBoxProvider {
    @Shadow @Final private ChunkGenerator field_31695;

    public ListTag getBoundingBoxes(Entity entity) {
        if (field_31695 instanceof BoundingBoxProvider) return ((BoundingBoxProvider) field_31695).getBoundingBoxes(entity);
        return new ListTag();
    }
}
