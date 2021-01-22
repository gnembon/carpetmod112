package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.AbstractTempleFeature;
import net.minecraft.world.gen.feature.MineshaftFeature;
import net.minecraft.world.gen.feature.OceanMonumentFeature;
import net.minecraft.world.gen.feature.StrongholdFeature;
import net.minecraft.world.gen.feature.VillageFeature;
import net.minecraft.world.gen.feature.WoodlandMansionFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OverworldChunkGenerator.class)
public class OverworldChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private AbstractTempleFeature templeFeature;
    @Shadow @Final private VillageFeature villageFeature;
    @Shadow @Final private StrongholdFeature strongholdFeature;
    @Shadow @Final private MineshaftFeature mineshaftFeature;
    @Shadow @Final private OceanMonumentFeature oceanMonumentFeature;
    @Shadow @Final private WoodlandMansionFeature woodlandMansionFeature;

    public ListTag getBoundingBoxes(Entity entity) {
        ListTag boxes = new ListTag();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(templeFeature, entity, CarpetClientMarkers.TEMPLE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(villageFeature, entity, CarpetClientMarkers.VILLAGE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(strongholdFeature, entity, CarpetClientMarkers.STRONGHOLD));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(mineshaftFeature, entity, CarpetClientMarkers.MINESHAFT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(oceanMonumentFeature, entity, CarpetClientMarkers.MONUMENT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(woodlandMansionFeature, entity, CarpetClientMarkers.MANSION));
        return boxes;
    }
}
