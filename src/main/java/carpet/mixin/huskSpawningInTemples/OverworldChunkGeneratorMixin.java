package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import carpet.mixin.accessors.StructureFeatureAccessor;
import net.minecraft.class_4301;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.AbstractTempleFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(OverworldChunkGenerator.class)
public class OverworldChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> HUSK_SPAWN_LIST = Collections.singletonList(new Biome.SpawnEntry(HuskEntity.class, 1, 1, 1));

    @Shadow @Final private AbstractTempleFeature field_25746;

    private static boolean isPyramid(AbstractTempleFeature temple, BlockPos pos) {
        StructureStart start = ((StructureFeatureAccessor) temple).invokeGetStructureAt(pos);
        if (!(start instanceof AbstractTempleFeature.class_5415) || start.getChildren().isEmpty()) return false;
        StructurePiece piece = start.getChildren().get(0);
        return piece instanceof class_4301.DesertTempleGenerator;
    }

    @Inject(method = "method_27343", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/AbstractTempleFeature;method_27800(Lnet/minecraft/util/math/BlockPos;)Z"))
    private void huskSpawningInTemples(SpawnGroup creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.huskSpawningInTemples && isPyramid(field_25746, pos)) {
            cir.setReturnValue(HUSK_SPAWN_LIST);
        }
    }
}
