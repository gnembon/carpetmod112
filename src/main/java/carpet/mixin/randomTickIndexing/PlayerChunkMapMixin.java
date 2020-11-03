package carpet.mixin.randomTickIndexing;

import carpet.carpetclient.CarpetClientRandomtickingIndexing;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkMap.class)
public class PlayerChunkMapMixin {
    @Shadow @Final private WorldServer world;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        // Sends updates to all subscribed players that want to get indexing of chunks Carpet-XCOM
        if (CarpetClientRandomtickingIndexing.sendUpdates(world)) {
            CarpetClientRandomtickingIndexing.sendRandomtickingChunkOrder(world, (PlayerChunkMap) (Object) this);
        }
    }
}
