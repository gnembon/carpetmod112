package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkGeneratorHell.class)
public class ChunkGeneratorHellMixin implements BoundingBoxProvider {
    @Shadow @Final private MapGenNetherBridge genNetherBridge;

    public NBTTagList getBoundingBoxes(Entity entity) {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(this.genNetherBridge, entity, CarpetClientMarkers.FORTRESS));
        return nbttaglist;
    }
}
