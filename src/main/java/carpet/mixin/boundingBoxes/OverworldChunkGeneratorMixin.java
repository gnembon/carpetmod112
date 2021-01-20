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
    @Shadow @Final private AbstractTempleFeature field_25746;
    @Shadow @Final private VillageFeature field_25744;
    @Shadow @Final private StrongholdFeature field_25743;
    @Shadow @Final private MineshaftFeature field_25745;
    @Shadow @Final private OceanMonumentFeature field_25718;
    @Shadow @Final private WoodlandMansionFeature field_25719;

    public ListTag getBoundingBoxes(Entity entity) {
        ListTag boxes = new ListTag();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25746, entity, CarpetClientMarkers.TEMPLE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25744, entity, CarpetClientMarkers.VILLAGE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25743, entity, CarpetClientMarkers.STRONGHOLD));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25745, entity, CarpetClientMarkers.MINESHAFT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25718, entity, CarpetClientMarkers.MONUMENT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(field_25719, entity, CarpetClientMarkers.MANSION));
        return boxes;
    }
}
