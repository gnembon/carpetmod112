package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import carpet.mixin.accessors.MapGenStructureAccessor;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(ChunkGeneratorOverworld.class)
public class ChunkGeneratorOverworldMixin {
    private static final List<Biome.SpawnListEntry> HUSK_SPAWN_LIST = Collections.singletonList(new Biome.SpawnListEntry(EntityHusk.class, 1, 1, 1));

    @Shadow @Final private MapGenScatteredFeature scatteredFeatureGenerator;

    private static boolean isPyramid(MapGenScatteredFeature temple, BlockPos pos) {
        StructureStart structurestart = ((MapGenStructureAccessor) temple).invokeGetStructureAt(pos);
        if (!(structurestart instanceof MapGenScatteredFeature.Start) || structurestart.getComponents().isEmpty()) return false;
        StructureComponent structurecomponent = structurestart.getComponents().get(0);
        return structurecomponent instanceof ComponentScatteredFeaturePieces.DesertPyramid;
    }

    @Inject(method = "getPossibleCreatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/structure/MapGenScatteredFeature;isSwampHut(Lnet/minecraft/util/math/BlockPos;)Z"))
    private void huskSpawningInTemples(EnumCreatureType creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnListEntry>> cir) {
        if (CarpetSettings.huskSpawningInTemples && isPyramid(scatteredFeatureGenerator, pos)) {
            cir.setReturnValue(HUSK_SPAWN_LIST);
        }
    }
}
