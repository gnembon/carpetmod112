package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.class_6380;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_6380.class)
public class PlayerChunkMapMixin {
    @Redirect(method = "method_33590", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkCache;method_33451()V"))
    private void queueUnloadAll(ServerChunkCache provider) {
        try {
            CarpetClientChunkLogger.setReason("Dimensional unloading due to no players");
            provider.method_33451();
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    @Redirect(method = "method_33589", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkCache;method_33448(Lnet/minecraft/world/chunk/Chunk;)V"))
    private void queueUnload(ServerChunkCache provider, Chunk chunk) {
        try {
            CarpetClientChunkLogger.setReason("Player leaving chunk, queuing unload");
            provider.method_33448(chunk);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
