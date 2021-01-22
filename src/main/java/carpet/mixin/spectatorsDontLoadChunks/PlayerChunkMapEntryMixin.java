package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.utils.ChunkLoading;
import net.minecraft.class_4615;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_4615.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean field_31800;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkCache;method_33452(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk loadChunk(ServerChunkCache provider, int x, int z) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ServerPlayerEntity player = ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.get();
            if (player != null && player.isSpectator()) return null;
        }
        try {
            CarpetClientChunkLogger.setReason("Player loading chunk");
            return provider.method_33452(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    // Return false to prevent client unloading the chunks when attempting to use spectate entitys near unloaded chunks. CARPET-XCOM
    @Redirect(method = "method_33569", at = @At(value = "FIELD", target = "Lnet/minecraft/class_4615;field_31800:Z"))
    private boolean sendPacket(class_4615 entry, ServerPlayerEntity player) {
        return field_31800 && (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator());
    }
}
