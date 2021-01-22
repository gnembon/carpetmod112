package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.EndChunkGenerator;
import net.minecraft.world.gen.feature.EndCityFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(EndChunkGenerator.class)
public class EndChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> spawnList = Collections.singletonList(new Biome.SpawnEntry(ShulkerEntity.class, 10, 4, 4));

    @Shadow @Final private EndCityFeature endCityFeature;
    @Shadow @Final private World world;

    @Inject(method = "getValidSpawnEntries", at = @At("HEAD"), cancellable = true)
    private void shulkerSpawning(SpawnGroup creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities && creatureType == SpawnGroup.MONSTER && endCityFeature.method_27842(pos)) {
            cir.setReturnValue(spawnList);
        }
    }

    @Inject(method = "generatureStructures", at = @At("HEAD"))
    private void recreateEndCityForShulkerSpawning(Chunk chunkIn, int x, int z, CallbackInfo ci) {
        if (CarpetSettings.shulkerSpawningInEndCities) this.endCityFeature.generate(this.world, x, z, null);
    }
}
