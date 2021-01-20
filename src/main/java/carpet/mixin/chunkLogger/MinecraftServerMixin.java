package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;save(Z)V", shift = At.Shift.BEFORE))
    private void onAutosaveStart(CallbackInfo ci) {
        if(CarpetClientChunkLogger.logger.enabled) CarpetClientChunkLogger.setReason("Autosave queuing chunks for unloading");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;save(Z)V", shift = At.Shift.AFTER))
    private void onAutosaveEnd(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
