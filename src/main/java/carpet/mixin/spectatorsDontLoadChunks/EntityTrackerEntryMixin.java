package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(method = "method_33559", at = @At("HEAD"), cancellable = true)
    private void spectatorsDontLoadChunks(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.spectatorsDontLoadChunks && player.isSpectator()) cir.setReturnValue(true);
    }
}
