package carpet.mixin.removeConnectionTimeout;

import carpet.CarpetSettings;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerLoginServer.class)
public abstract class NetHandlerLoginServerMixin {
    private long connectionSystemTimer;

    @Shadow public abstract void disconnect(ITextComponent reason);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void determineTimeout(MinecraftServer serverIn, NetworkManager networkManagerIn, CallbackInfo ci) {
        // Get current time as player starts logging in, 30 sec as timeout timer CARPET-XCOM
        connectionSystemTimer = System.currentTimeMillis() - 30000;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 600))
    private int disableVanillaTimeout(int tickLimit) {
        return -1;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void betterTimout(CallbackInfo ci) {
        // Hard swap to system time check to prevent players timing out when tick warping CARPET-XCOM
        if (!CarpetSettings.removeConnectionTimeout && connectionSystemTimer > System.currentTimeMillis()) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.slow_login"));
        }
    }
}
