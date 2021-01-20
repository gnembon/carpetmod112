package carpet.mixin.removeConnectionTimeout;

import carpet.CarpetSettings;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    private long connectionSystemTimer;

    @Shadow public abstract void disconnect(Text reason);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void determineTimeout(MinecraftServer serverIn, ClientConnection networkManagerIn, CallbackInfo ci) {
        // Get current time as player starts logging in, 30 sec as timeout timer CARPET-XCOM
        connectionSystemTimer = System.currentTimeMillis() - 30000;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600))
    private int disableVanillaTimeout(int tickLimit) {
        return -1;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void betterTimout(CallbackInfo ci) {
        // Hard swap to system time check to prevent players timing out when tick warping CARPET-XCOM
        if (!CarpetSettings.removeConnectionTimeout && connectionSystemTimer > System.currentTimeMillis()) {
            this.disconnect(new TranslatableText("multiplayer.disconnect.slow_login"));
        }
    }
}
