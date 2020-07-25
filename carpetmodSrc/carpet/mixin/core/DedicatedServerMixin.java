package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;setOnlineMode(Z)V", shift = At.Shift.AFTER))
    private void onServerLoaded(CallbackInfoReturnable<Boolean> cir) {
        CarpetServer.onServerLoaded((MinecraftServer) (Object) this);
    }
}
