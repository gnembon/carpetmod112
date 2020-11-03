package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkProviderServer.class)
public class ChunkProviderServerMixin implements BoundingBoxProvider {
    @Shadow @Final public IChunkGenerator chunkGenerator;

    public NBTTagList getBoundingBoxes(Entity entity) {
        if (chunkGenerator instanceof BoundingBoxProvider) return ((BoundingBoxProvider) chunkGenerator).getBoundingBoxes(entity);
        return new NBTTagList();
    }
}
