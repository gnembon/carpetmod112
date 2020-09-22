package carpet.mixin.loggers;

import carpet.logging.logHelpers.DebugLogHelper;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(method = "removeFromTrackedPlayers", at = @At("HEAD"))
    private void invisDebug(EntityPlayerMP playerMP, CallbackInfo ci) {
        DebugLogHelper.invisDebug(() -> "r1: " + playerMP, true);
    }
}
