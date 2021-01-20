package carpet.mixin.duplicationFixLogout;

import carpet.CarpetSettings;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onDigging(PlayerActionC2SPacket packetIn, CallbackInfo ci) {
        // Prevent player preforming actions after disconnecting. CARPET-XCOM
        if (CarpetSettings.duplicationFixLogout && player.isDisconnected()) ci.cancel();
    }
}
