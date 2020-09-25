package carpet.mixin.randomTickIndexing;

import carpet.carpetclient.CarpetClientRandomtickingIndexing;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkMapEntry.class)
public class PlayerChunkMapEntryMixin {
    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void onAdd(EntityPlayerMP player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void onRemove(EntityPlayerMP player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }
}
