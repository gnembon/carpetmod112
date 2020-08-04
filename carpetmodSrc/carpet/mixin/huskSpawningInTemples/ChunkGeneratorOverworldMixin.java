package carpet.mixin.huskSpawningInTemples;

import carpet.mixin.accessors.MapGenStructureAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkGeneratorOverworld.class)
public class ChunkGeneratorOverworldMixin {
    private static boolean isPyramid(MapGenScatteredFeature temple, BlockPos pos) {
        StructureStart structurestart = ((MapGenStructureAccessor) temple).invokeGetStructureAt(pos);
        if (!(structurestart instanceof MapGenScatteredFeature.Start) || structurestart.getComponents().isEmpty()) return false;
        StructureComponent structurecomponent = structurestart.getComponents().get(0);
        return structurecomponent instanceof ComponentScatteredFeaturePieces.DesertPyramid;
    }
}
