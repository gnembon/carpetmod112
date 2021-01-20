package carpet.mixin.elytraCheckFix;

import carpet.CarpetSettings;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    // Remove the falling check as in 1.15 CARPET-XCOM
    @Redirect(method = "onClientCommand", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;velocityY:D"))
    private double motionForElytraCheck(ServerPlayerEntity player) {
        return CarpetSettings.elytraCheckFix ? -1 : player.velocityY;
    }
}
