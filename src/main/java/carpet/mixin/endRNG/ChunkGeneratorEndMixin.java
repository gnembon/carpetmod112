package carpet.mixin.endRNG;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ChunkGeneratorEnd.class)
public class ChunkGeneratorEndMixin implements ExtendedChunkGeneratorEnd {
    @Shadow @Final private Random rand;
    private long lastRandomSeed = 0;
    private boolean randomSeedUsed = false;

    @Redirect(method = "generateChunk", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"))
    private void onGenerate(Random random, long seed) {
        if (CarpetSettings.endChunkSeed != 0) seed = CarpetSettings.endChunkSeed;
        random.setSeed(seed);
        this.lastRandomSeed = seed;
        this.randomSeedUsed = false;
    }

    @Inject(method = "populate", at = @At("HEAD"))
    private void onPopulate(int x, int z, CallbackInfo ci) {
        if (CarpetSettings.endChunkSeed != 0) {
            this.rand.setSeed(CarpetSettings.endChunkSeed);
        }
    }

    @Inject(method = "populate", at = @At("RETURN"))
    private void onPopulateEnd(int x, int z, CallbackInfo ci) {
        this.randomSeedUsed = true;
    }

    @Override
    public void setEndChunkSeed(long seed) {
        this.rand.setSeed(seed);
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
