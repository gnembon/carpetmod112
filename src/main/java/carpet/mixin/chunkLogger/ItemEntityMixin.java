package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract String method_29611();

    @Inject(method = "method_34456", at = @At("HEAD"))
    private void onHandleWaterMovementStart(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.setReason(() -> "Item checking if pushed by water: " + method_29611());
    }

    @Inject(method = "method_34456", at = @At("RETURN"))
    private void onHandleWaterMovementEnd(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
