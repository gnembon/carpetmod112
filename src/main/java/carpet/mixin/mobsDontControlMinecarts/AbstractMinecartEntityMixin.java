package carpet.mixin.mobsDontControlMinecarts;

import carpet.CarpetSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Redirect(method = "moveOnRail", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;forwardSpeed:F"))
    private float mobsDontControlMinecarts(LivingEntity entity) {
        if (!CarpetSettings.mobsDontControlMinecarts || entity instanceof PlayerEntity) return entity.forwardSpeed;
        return 0;
    }
}
