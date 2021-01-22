package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.NetherChunkGenerator;
import net.minecraft.world.gen.feature.NetherFortressFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetherChunkGenerator.class)
public class NetherChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private NetherFortressFeature netherFortressFeature;

    @Override
    public ListTag getBoundingBoxes(Entity entity) {
        ListTag boxes = new ListTag();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(this.netherFortressFeature, entity, CarpetClientMarkers.FORTRESS));
        return boxes;
    }
}
