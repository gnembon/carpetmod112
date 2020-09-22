package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processCustomPayload", at = @At("TAIL"))
    private void handleCarpetCustomPayloads(CPacketCustomPayload packet, CallbackInfo ci) {
        if (!packet.getChannelName().startsWith("MC|")) {
            CarpetServer.pluginChannels.process(player, packet);
        }
    }
}
