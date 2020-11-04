package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.structure.MapGenEndCity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(ChunkGeneratorEnd.class)
public class ChunkGeneratorEndMixin {
    private static final List<Biome.SpawnListEntry> spawnList = Collections.singletonList(new Biome.SpawnListEntry(EntityShulker.class, 10, 4, 4));

    @Shadow @Final private MapGenEndCity endCityGen;
    @Shadow @Final private World world;

    @Inject(method = "getPossibleCreatures", at = @At("HEAD"), cancellable = true)
    private void shulkerSpawning(EnumCreatureType creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnListEntry>> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities && creatureType == EnumCreatureType.MONSTER && endCityGen.isInsideStructure(pos)) {
            cir.setReturnValue(spawnList);
        }
    }

    @Inject(method = "recreateStructures", at = @At("HEAD"))
    private void recreateEndCityForShulkerSpawning(Chunk chunkIn, int x, int z, CallbackInfo ci) {
        if (CarpetSettings.shulkerSpawningInEndCities) this.endCityGen.generate(this.world, x, z, null);
    }
}
