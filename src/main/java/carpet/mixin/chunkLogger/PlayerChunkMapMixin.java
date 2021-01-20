package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.class_6380;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_6380.class)
public class PlayerChunkMapMixin {
    @Redirect(method = "method_33590", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;method_33451()V"))
    private void queueUnloadAll(ServerChunkManager provider) {
        try {
            CarpetClientChunkLogger.setReason("Dimensional unloading due to no players");
            provider.method_33451();
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    @Redirect(method = "method_33589", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;method_33448(Lnet/minecraft/world/chunk/WorldChunk;)V"))
    private void queueUnload(ServerChunkManager provider, WorldChunk chunk) {
        try {
            CarpetClientChunkLogger.setReason("Player leaving chunk, queuing unload");
            provider.method_33448(chunk);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
