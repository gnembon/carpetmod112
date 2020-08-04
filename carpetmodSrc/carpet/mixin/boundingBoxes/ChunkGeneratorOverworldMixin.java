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
        nbttaglist.appendTag(scatteredFeatureGenerator.getBoundingBoxes(entity, CarpetClientMarkers.TEMPLE));
        nbttaglist.appendTag(villageGenerator.getBoundingBoxes(entity, CarpetClientMarkers.VILLAGE));
        nbttaglist.appendTag(strongholdGenerator.getBoundingBoxes(entity, CarpetClientMarkers.STRONGHOLD));
        nbttaglist.appendTag(mineshaftGenerator.getBoundingBoxes(entity, CarpetClientMarkers.MINESHAFT));
        nbttaglist.appendTag(oceanMonumentGenerator.getBoundingBoxes(entity, CarpetClientMarkers.MONUMENT));
        nbttaglist.appendTag(woodlandMansionGenerator.getBoundingBoxes(entity, CarpetClientMarkers.MANSION));
        return nbttaglist;
    }
}
