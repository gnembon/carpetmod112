package carpet.mixin.loggers;

import carpet.logging.logHelpers.PacketCounter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class NetworkManagerMixin {
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onReceive(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        PacketCounter.totalIn++;
    }

    @Inject(method = "method_32196", at = @At("HEAD"))
    private void onSend(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void>>[] futureListeners, CallbackInfo ci) {
        PacketCounter.totalOut++;
    }
}
