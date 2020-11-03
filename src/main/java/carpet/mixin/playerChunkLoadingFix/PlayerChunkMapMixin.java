package carpet.mixin.playerChunkLoadingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Fix the player chunk map truncation in negative coords causing offsets in chunk loading CARPET-XCOM
@Mixin(PlayerChunkMap.class)
public class PlayerChunkMapMixin {
    @Redirect(method = {
        "addPlayer",
        "updateMovingPlayer"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;posX:D", ordinal = 0))
    private double getPosX(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.posX) : player.posX;
    }

    @Redirect(method = {
        "addPlayer",
        "updateMovingPlayer"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;posZ:D", ordinal = 0))
    private double getPosZ(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.posZ) : player.posZ;
    }

    @Redirect(method = "removePlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;managedPosX:D", ordinal = 0))
    private double getManagedPosX(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.managedPosX) : player.managedPosX;
    }

    @Redirect(method = "removePlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;managedPosZ:D", ordinal = 0))
    private double getManagedPosZ(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.managedPosZ) : player.managedPosZ;
    }

    @Redirect(method = "updateMovingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;managedPosX:D", ordinal = 1))
    private double getManagedPosX2(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.managedPosX) : player.managedPosX;
    }

    @Redirect(method = "updateMovingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;managedPosZ:D", ordinal = 1))
    private double getManagedPosZ2(EntityPlayerMP player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.managedPosZ) : player.managedPosZ;
    }
}
