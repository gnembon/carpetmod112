package carpet.mixin.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    // Applies noClip, using isPlayerSleeping because pistonClippingFix already redirects noClip
    @Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isPlayerSleeping()Z", ordinal = 2))
    private boolean isPlayerSleeping(EntityPlayerMP player) {
        return player.isPlayerSleeping() || (CarpetSettings.creativeNoClip && player.isCreative());
    }
}
