package carpet.mixin.optimizedCollisionCancellations;

import carpet.CarpetSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract boolean isPushable();

    @Inject(method = "tickCramming", at = @At("HEAD"), cancellable = true)
    private void optimizedCollisionCancellations(CallbackInfo ci) {
        // Collision calculations are cancelled if isPushable() is false. Return here to lag optimize the collision code. CARPET-XCOM
        //noinspection ConstantConditions
        if(CarpetSettings.optimizedCollisionCancellations && !isPushable() && !((Object) this instanceof EnderDragonEntity)) {
            ci.cancel();
        }
    }
}
