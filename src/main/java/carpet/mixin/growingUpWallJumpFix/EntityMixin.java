package carpet.mixin.growingUpWallJumpFix;

import carpet.CarpetSettings;
import carpet.helpers.BabyGrowingUp;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "setDimensions", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;width:F", ordinal = 1), cancellable = true)
    private void growingUpWallJumpFix(float width, float height, CallbackInfo ci) {
        if (CarpetSettings.growingUpWallJumpFix) {
            BabyGrowingUp.carpetSetSize((Entity) (Object) this, width, height);
            ci.cancel();
        }
    }
}
