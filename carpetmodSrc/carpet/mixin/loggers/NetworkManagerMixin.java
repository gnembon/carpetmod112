package carpet.mixin.loggers;

import carpet.logging.logHelpers.PacketCounter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onReceive(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        PacketCounter.totalIn++;
    }

    @Inject(method = "dispatchPacket", at = @At("HEAD"))
    private void onSend(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void>>[] futureListeners, CallbackInfo ci) {
        PacketCounter.totalOut++;
    }
}
