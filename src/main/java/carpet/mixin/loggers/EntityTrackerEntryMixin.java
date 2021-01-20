package carpet.mixin.loggers;

import carpet.logging.logHelpers.DebugLogHelper;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(method = "method_33549", at = @At("HEAD"))
    private void invisDebug(ServerPlayerEntity playerMP, CallbackInfo ci) {
        DebugLogHelper.invisDebug(() -> "r1: " + playerMP, true);
    }
}
