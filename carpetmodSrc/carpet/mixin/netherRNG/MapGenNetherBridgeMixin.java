package carpet.mixin.netherRNG;

import carpet.CarpetSettings;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(MapGenNetherBridge.class)
public abstract class MapGenNetherBridgeMixin extends MapGenStructure {
    @Redirect(method = "canSpawnStructureAtCoords", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V", remap = false))
    private void setSeed(Random random, long seed) {
        if (CarpetSettings.netherRNG) {
            world.rand.setSeed(seed);
        }
        random.setSeed(seed);
    }
}
