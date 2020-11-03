package carpet.mixin.optimizedTNT;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import carpet.mixin.accessors.ExplosionAccessor;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Inject(method = "doExplosionA", at = @At("HEAD"), cancellable = true)
    private void onExplosionA(CallbackInfo ci) {
        if (CarpetSettings.optimizedTNT) {
            OptimizedTNT.doExplosionA((ExplosionAccessor) this);
            ci.cancel();
        }
    }

    @Inject(method = "doExplosionB", at = @At("HEAD"), cancellable = true)
    private void onExplosionB(boolean spawnParticles, CallbackInfo ci) {
        if (CarpetSettings.optimizedTNT) {
            OptimizedTNT.doExplosionB((ExplosionAccessor) this, spawnParticles);
            ci.cancel();
        }
    }
}
