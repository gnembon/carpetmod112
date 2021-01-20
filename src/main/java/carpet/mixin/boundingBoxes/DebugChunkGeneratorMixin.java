package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DebugChunkGenerator.class)
public class DebugChunkGeneratorMixin implements BoundingBoxProvider {
    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        return new ListTag();
    }
}
