package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerNetworkIo.class)
public class ServerNetworkIoMixin {
    // Earlier than MinecraftServer.<init>()V @RETURN
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftServer server, CallbackInfo ci) {
        CarpetServer.init(server);
    }
}
