package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.Entity;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract String method_29611();

    @Inject(method = "method_34456", at = @At("HEAD"))
    private void onWaterMovementStart(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.setReason(() -> "Entity checking if pushed by water: " + method_29611());
    }

    @Inject(method = "method_34456", at = @At("RETURN"))
    private void onWaterMovementEnd(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }

    @Redirect(method = "method_34471", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/PortalForcer;method_26217(Lnet/minecraft/entity/Entity;F)Z"))
    private boolean placeInExistingPortal(PortalForcer teleporter, Entity entityIn, float rotationYaw) {
        try {
            CarpetClientChunkLogger.setReason(() -> "Entity going through nether portal: " + method_29611());
            return teleporter.method_26217(entityIn, rotationYaw);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
