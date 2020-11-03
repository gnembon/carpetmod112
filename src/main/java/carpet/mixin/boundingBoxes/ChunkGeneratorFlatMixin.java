package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.structure.MapGenStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ChunkGeneratorFlat.class)
public class ChunkGeneratorFlatMixin implements BoundingBoxProvider {
    @Shadow
    @Final
    private Map<String, MapGenStructure> structureGenerators;

    public NBTTagList getBoundingBoxes(Entity entity) {
        NBTTagList nbttaglist = new NBTTagList();
        for (Map.Entry<String, MapGenStructure> e : structureGenerators.entrySet()) {
            nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(e.getValue(), entity, 1));
        }
        return nbttaglist;
    }
}
