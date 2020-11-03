package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkGeneratorOverworld.class)
public class ChunkGeneratorOverworldMixin implements BoundingBoxProvider {
    @Shadow @Final private MapGenScatteredFeature scatteredFeatureGenerator;
    @Shadow @Final private MapGenVillage villageGenerator;
    @Shadow @Final private MapGenStronghold strongholdGenerator;
    @Shadow @Final private MapGenMineshaft mineshaftGenerator;
    @Shadow @Final private StructureOceanMonument oceanMonumentGenerator;
    @Shadow @Final public WoodlandMansion woodlandMansionGenerator;

    public NBTTagList getBoundingBoxes(Entity entity) {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(scatteredFeatureGenerator, entity, CarpetClientMarkers.TEMPLE));
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(villageGenerator, entity, CarpetClientMarkers.VILLAGE));
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(strongholdGenerator, entity, CarpetClientMarkers.STRONGHOLD));
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(mineshaftGenerator, entity, CarpetClientMarkers.MINESHAFT));
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(oceanMonumentGenerator, entity, CarpetClientMarkers.MONUMENT));
        nbttaglist.appendTag(CarpetClientMarkers.getBoundingBoxes(woodlandMansionGenerator, entity, CarpetClientMarkers.MANSION));
        return nbttaglist;
    }
}
