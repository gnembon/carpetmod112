package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerChunkMap.class)
public class PlayerChunkMapMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;queueUnloadAll()V"))
    private void queueUnloadAll(ChunkProviderServer provider) {
        try {
            CarpetClientChunkLogger.setReason("Dimensional unloading due to no players");
            provider.queueUnloadAll();
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    @Redirect(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;queueUnload(Lnet/minecraft/world/chunk/Chunk;)V"))
    private void queueUnload(ChunkProviderServer provider, Chunk chunk) {
        try {
            CarpetClientChunkLogger.setReason("Player leaving chunk, queuing unload");
            provider.queueUnload(chunk);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
