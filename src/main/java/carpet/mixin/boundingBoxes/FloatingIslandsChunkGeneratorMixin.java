package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.FloatingIslandsChunkGenerator;
import net.minecraft.world.gen.feature.EndCityFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FloatingIslandsChunkGenerator.class)
public class FloatingIslandsChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private EndCityFeature field_25761;

    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        ListTag nbttaglist = new ListTag();
        nbttaglist.add(CarpetClientMarkers.getBoundingBoxes(field_25761, entity, CarpetClientMarkers.END_CITY));
        return nbttaglist;
    }
}
