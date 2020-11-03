package carpet.mixin.disableSpawnChunks;

import carpet.CarpetSettings;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow protected abstract void clearCurrentTask();

    @Inject(method = "initialWorldChunkLoad", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", remap = false), cancellable = true)
    private void disableSpawnChunks(CallbackInfo ci) {
        if (CarpetSettings.disableSpawnChunks) {
            clearCurrentTask();
            ci.cancel();
        }
    }
}
