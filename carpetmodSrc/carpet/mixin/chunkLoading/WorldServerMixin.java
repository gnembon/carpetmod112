package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin {
    @Shadow public abstract ChunkProviderServer getChunkProvider();

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;contains(II)Z"))
    private boolean isInPlayerChunkMap(PlayerChunkMap map, int chunkX, int chunkZ) {
        PlayerChunkMapEntry entry =  map.getEntry(chunkX, chunkZ);
        if (entry != null && CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            Chunk chunk = entry.getChunk();
            getChunkProvider().queueUnload(chunk);
            chunk.unloadQueued = false;
            return true;
        }
        return false;
    }
}
