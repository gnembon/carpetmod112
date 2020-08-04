package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.structure.MapGenEndCity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkGeneratorEnd.class)
public class ChunkGeneratorEndMixin implements BoundingBoxProvider {
    @Shadow @Final private MapGenEndCity endCityGen;

    public NBTTagList getBoundingBoxes(Entity entity) {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.appendTag(endCityGen.getBoundingBoxes(entity, CarpetClientMarkers.END_CITY));
        return nbttaglist;
    }
}
