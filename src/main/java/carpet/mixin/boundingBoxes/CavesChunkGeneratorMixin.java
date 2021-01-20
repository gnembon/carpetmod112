package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.CavesChunkGenerator;
import net.minecraft.world.gen.feature.NetherFortressFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CavesChunkGenerator.class)
public class CavesChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private NetherFortressFeature field_25689;

    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        ListTag boxes = new ListTag();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(this.field_25689, entity, CarpetClientMarkers.FORTRESS));
        return boxes;
    }
}
