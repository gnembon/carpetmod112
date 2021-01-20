package carpet.mixin.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    // Applies noClip, using isPlayerSleeping because pistonClippingFix already redirects noClip
    @Redirect(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;method_34726()Z", ordinal = 2))
    private boolean isPlayerSleeping(ServerPlayerEntity player) {
        return player.method_34726() || (CarpetSettings.creativeNoClip && player.isCreative());
    }
}
