package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "playerLoggedIn", at = @At("RETURN"))
    private void onPlayerConnect(EntityPlayerMP player, CallbackInfo ci) {
        CarpetServer.playerConnected(player);
    }

    @Inject(method = "playerLoggedOut", at = @At("HEAD"))
    private void onPlayerDisconnect(EntityPlayerMP player, CallbackInfo ci) {
        CarpetServer.playerDisconnected(player);
    }
}
