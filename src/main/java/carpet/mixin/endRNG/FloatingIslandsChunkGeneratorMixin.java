package carpet.mixin.endRNG;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedFloatingIslandsChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.world.gen.chunk.FloatingIslandsChunkGenerator;

@Mixin(FloatingIslandsChunkGenerator.class)
public class FloatingIslandsChunkGeneratorMixin implements ExtendedFloatingIslandsChunkGenerator {
    @Shadow @Final private Random field_25754;
    private long lastRandomSeed = 0;
    private boolean randomSeedUsed = false;

    @Redirect(method = "method_27339", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"))
    private void onGenerate(Random random, long seed) {
        if (CarpetSettings.endChunkSeed != 0) seed = CarpetSettings.endChunkSeed;
        random.setSeed(seed);
        this.lastRandomSeed = seed;
        this.randomSeedUsed = false;
    }

    @Inject(method = "method_27344", at = @At("HEAD"))
    private void onPopulate(int x, int z, CallbackInfo ci) {
        if (CarpetSettings.endChunkSeed != 0) {
            this.field_25754.setSeed(CarpetSettings.endChunkSeed);
        }
    }

    @Inject(method = "method_27344", at = @At("RETURN"))
    private void onPopulateEnd(int x, int z, CallbackInfo ci) {
        this.randomSeedUsed = true;
    }

    @Override
    public void setEndChunkSeed(long seed) {
        this.field_25754.setSeed(seed);
    }

    @Override
    public long getLastRandomSeed() {
        return lastRandomSeed;
    }

    @Override
    public void setRandomSeedUsed(boolean used) {
        randomSeedUsed = used;
    }

    @Override
    public boolean wasRandomSeedUsed() {
        return randomSeedUsed;
    }
}
