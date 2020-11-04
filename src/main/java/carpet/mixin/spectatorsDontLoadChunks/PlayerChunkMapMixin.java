package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(PlayerChunkMap.class)
public abstract class PlayerChunkMapMixin {
    @Shadow private static long getIndex(int chunkX, int chunkZ) { throw new AbstractMethodError(); }
    @Shadow @Final private Long2ObjectMap<PlayerChunkMapEntry> entryMap;
    @Shadow @Final private List<PlayerChunkMapEntry> entriesWithoutChunks;
    @Shadow @Final private List<PlayerChunkMapEntry> pendingSendToPlayers;
    @Shadow @Final private List<PlayerChunkMapEntry> entries;

    @Redirect(method = {
        "addPlayer",
        "updateMovingPlayer"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;getOrCreateEntry(II)Lnet/minecraft/server/management/PlayerChunkMapEntry;"))
    private PlayerChunkMapEntry getOrCreateHooks(PlayerChunkMap map, int chunkX, int chunkZ, EntityPlayerMP player) {
        return getOrCreateEntry(chunkX, chunkZ, player);
    }

    @Inject(method = "setPlayerViewRadius", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;posX:D"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void capturePlayer(int radius, CallbackInfo ci, int i, List<EntityPlayerMP> list, Iterator<EntityPlayerMP> iterator, EntityPlayerMP player) {
        ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
    }

    @Redirect(method = "setPlayerViewRadius", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;getOrCreateEntry(II)Lnet/minecraft/server/management/PlayerChunkMapEntry;"))
    private PlayerChunkMapEntry getOrCreateOnSetRadius(PlayerChunkMap map, int chunkX, int chunkZ) {
        return getOrCreateEntry(chunkX, chunkZ, null);
    }

    @Unique private PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ, EntityPlayerMP player) {
        long i = getIndex(chunkX, chunkZ);
        PlayerChunkMapEntry entry = this.entryMap.get(i);

        if (entry == null) {
            if (player != null) ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
            entry = new PlayerChunkMapEntry((PlayerChunkMap) (Object) this, chunkX, chunkZ);
            ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(null);
            if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                this.entryMap.put(i, entry);
            this.entries.add(entry);

            if (entry.getChunk() == null) {
                if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                    this.entriesWithoutChunks.add(entry);
            }

            if (!entry.sendToPlayers()) {
                this.pendingSendToPlayers.add(entry);
            }
        }
        return entry;
    }
}
