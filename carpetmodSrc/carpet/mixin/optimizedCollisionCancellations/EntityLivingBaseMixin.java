package carpet.mixin.optimizedCollisionCancellations;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin  {
    @Shadow public abstract boolean canBePushed();

    @Inject(method = "collideWithNearbyEntities", at = @At("HEAD"), cancellable = true)
    private void optimizedCollisionCancellations(CallbackInfo ci) {
        // Collision calculations are cancelled if canBePushed() is false. Return here to lag optimize the collision code. CARPET-XCOM
        //noinspection ConstantConditions
        if(CarpetSettings.optimizedCollisionCancellations && !canBePushed() && !((Object) this instanceof EntityDragon)) {
            ci.cancel();
        }
    }
}
