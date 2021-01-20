package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private Map<String, StructureFeature> field_25663;

    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        ListTag boxes = new ListTag();
        for (Map.Entry<String, StructureFeature> e : field_25663.entrySet()) {
            boxes.add(CarpetClientMarkers.getBoundingBoxes(e.getValue(), entity, 1));
        }
        return boxes;
    }
}
