package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "method_33721", at = @At("RETURN"))
    private void onPlayerConnect(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetServer.playerConnected(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetServer.playerDisconnected(player);
    }
}
