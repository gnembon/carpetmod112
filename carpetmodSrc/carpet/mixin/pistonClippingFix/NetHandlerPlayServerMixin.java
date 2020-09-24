package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPlayerPistonClipping;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "update", at = @At("TAIL"))
    private void updatePistonClippingCounter(CallbackInfo ci) {
        // [CM] PistonClippingFix -- Check if counter is above 0 and decrement
        ExtendedPlayerPistonClipping playerClipping = (ExtendedPlayerPistonClipping) player;
        if (CarpetSettings.pistonClippingFix > 0 && playerClipping.getClippingCounter() > 0) {
            playerClipping.setClippingCounter(playerClipping.getClippingCounter() - 1);
        } else {
            playerClipping.setClippingCounter(0);
        }
    }

    // [CM] PistonClippingFix -- Added PistonClippingcounter check to the if statement
    @Redirect(method = "processPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;noClip:Z"))
    private boolean isNoClip(EntityPlayerMP player) {
        return player.noClip || ((ExtendedPlayerPistonClipping) player).getClippingCounter() > 0;
    }
}
