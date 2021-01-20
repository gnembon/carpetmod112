package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.FloatingIslandsChunkGenerator;
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

@Mixin(FloatingIslandsChunkGenerator.class)
public class FloatingIslandsChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> spawnList = Collections.singletonList(new Biome.SpawnEntry(ShulkerEntity.class, 10, 4, 4));

    @Shadow @Final private EndCityFeature field_25761;
    @Shadow @Final private World field_25758;

    @Inject(method = "method_27343", at = @At("HEAD"), cancellable = true)
    private void shulkerSpawning(EntityCategory creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities && creatureType == EntityCategory.MONSTER && field_25761.method_27842(pos)) {
            cir.setReturnValue(spawnList);
        }
    }

    @Inject(method = "method_27345", at = @At("HEAD"))
    private void recreateEndCityForShulkerSpawning(WorldChunk chunkIn, int x, int z, CallbackInfo ci) {
        if (CarpetSettings.shulkerSpawningInEndCities) this.field_25761.method_27580(this.field_25758, x, z, null);
    }
}
