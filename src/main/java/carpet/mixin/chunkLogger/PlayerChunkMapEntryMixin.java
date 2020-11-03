package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkMapEntry.class)
public class PlayerChunkMapEntryMixin {
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final private ChunkPos pos;

    @Inject(method = "addPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
    private void onAdd(EntityPlayerMP player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.playerChunkMap.getWorldServer(), pos.x, pos.z, CarpetClientChunkLogger.Event.PLAYER_ENTERS);
    }

    @Inject(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;removeEntry(Lnet/minecraft/server/management/PlayerChunkMapEntry;)V"))
    private void onRemove(EntityPlayerMP player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.playerChunkMap.getWorldServer(), pos.x, pos.z, CarpetClientChunkLogger.Event.PLAYER_LEAVES);
    }

    @Redirect(method = "providePlayerChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;provideChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk provideChunk(ChunkProviderServer provider, int x, int z) {
        try {
            CarpetClientChunkLogger.setReason("Player loading new chunks and generating");
            return provider.provideChunk(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
