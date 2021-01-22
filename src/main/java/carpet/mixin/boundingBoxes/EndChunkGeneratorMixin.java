package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.EndChunkGenerator;
import net.minecraft.world.gen.feature.EndCityFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EndChunkGenerator.class)
public class EndChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private EndCityFeature endCityFeature;

    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        ListTag nbttaglist = new ListTag();
        nbttaglist.add(CarpetClientMarkers.getBoundingBoxes(endCityFeature, entity, CarpetClientMarkers.END_CITY));
        return nbttaglist;
    }
}
