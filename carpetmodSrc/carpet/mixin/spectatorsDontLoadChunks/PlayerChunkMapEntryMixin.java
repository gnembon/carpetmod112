package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.utils.ChunkLoading;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerChunkMapEntry.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean sentToPlayers;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk loadChunk(ChunkProviderServer provider, int x, int z) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            EntityPlayerMP player = ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.get();
            if (player != null && player.isSpectator()) return null;
        }
        try {
            CarpetClientChunkLogger.setReason("Player loading chunk");
            return provider.loadChunk(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    // Return false to prevent client unloading the chunks when attempting to use spectate entitys near unloaded chunks. CARPET-XCOM
    @Redirect(method = "removePlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/management/PlayerChunkMapEntry;sentToPlayers:Z"))
    private boolean sendPacket(PlayerChunkMapEntry entry, EntityPlayerMP player) {
        return sentToPlayers && (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator());
    }
}
