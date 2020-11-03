package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkGeneratorDebug.class)
public class ChunkGeneratorDebugMixin implements BoundingBoxProvider {
    @Override
    public NBTTagList getBoundingBoxes(Entity entity) {
        return new NBTTagList();
    }
}
