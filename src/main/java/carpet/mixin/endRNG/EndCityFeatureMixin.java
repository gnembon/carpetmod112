package carpet.mixin.endRNG;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.EndCityFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EndCityFeature.class)
public class EndCityFeatureMixin {
    @Redirect(method = "method_27834", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_25976(III)Ljava/util/Random;"))
    private Random endRNG(World world, int x, int z, int seed) {
        if (CarpetSettings.endRNG) return CarpetServer.setRandomSeed(x, z, seed);
        return world.method_25976(x, z, seed);
    }
}
