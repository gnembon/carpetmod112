package carpet.mixin.structureBlockLimit;

import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerChunkMapEntry.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean sentToPlayers;

    @Shadow @Final private List<EntityPlayerMP> players;

    @Inject(method = "sendBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getUpdatePacket()Lnet/minecraft/network/play/server/SPacketUpdateTileEntity;"), cancellable = true)
    private void sendPlayerSensitiveBlockEntity(TileEntity be, CallbackInfo ci) {
        if (be instanceof IPlayerSensitiveTileEntity) {
            if (sentToPlayers) {
                for (EntityPlayerMP player : players) {
                    SPacketUpdateTileEntity packet = ((IPlayerSensitiveTileEntity) be).getUpdatePacketPlayerSensitive(player);
                    if (packet != null) player.connection.sendPacket(packet);
                }
            }
            ci.cancel();
        }
    }
}
