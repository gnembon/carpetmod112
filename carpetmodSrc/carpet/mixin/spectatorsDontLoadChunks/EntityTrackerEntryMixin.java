package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(method = "isPlayerWatchingThisChunk", at = @At("HEAD"), cancellable = true)
    private void spectatorsDontLoadChunks(EntityPlayerMP player, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.spectatorsDontLoadChunks && player.isSpectator()) cir.setReturnValue(true);
    }
}
