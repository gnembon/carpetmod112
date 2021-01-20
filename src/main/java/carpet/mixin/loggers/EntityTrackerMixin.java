package carpet.mixin.loggers;

import carpet.logging.logHelpers.DebugLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTracker.class)
public class EntityTrackerMixin {
    @Inject(method = "method_33437", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private void invisDebug(Entity entityIn, CallbackInfo ci) {
        DebugLogHelper.invisDebug(() -> "t1: " + entityIn, true);
    }
}
