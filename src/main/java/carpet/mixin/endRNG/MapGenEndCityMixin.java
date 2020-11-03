package carpet.mixin.endRNG;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenEndCity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(MapGenEndCity.class)
public class MapGenEndCityMixin {
    @Redirect(method = "canSpawnStructureAtCoords", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setRandomSeed(III)Ljava/util/Random;"))
    private Random endRNG(World world, int x, int z, int seed) {
        if (CarpetSettings.endRNG) return CarpetServer.setRandomSeed(x, z, seed);
        return world.setRandomSeed(x, z, seed);
    }
}
