package carpet.mixin.duplicationFixLogout;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processPlayerDigging", at = @At("HEAD"), cancellable = true)
    private void onDigging(CPacketPlayerDigging packetIn, CallbackInfo ci) {
        // Prevent player preforming actions after disconnecting. CARPET-XCOM
        if (CarpetSettings.duplicationFixLogout && player.hasDisconnected()) ci.cancel();
    }
}
