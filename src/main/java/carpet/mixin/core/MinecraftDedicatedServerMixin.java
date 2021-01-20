package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {
    @Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V", shift = At.Shift.AFTER))
    private void onServerLoaded(CallbackInfoReturnable<Boolean> cir) {
        CarpetServer.onServerLoaded((MinecraftServer) (Object) this);
    }
}
