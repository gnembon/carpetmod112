package carpet.mixin.playersTurningInvisibleFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.WorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "recreatePlayerEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
    private void removeFromChunk(EntityPlayerMP player, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            player.getServerWorld().getChunk(player.chunkCoordX, player.chunkCoordZ).removeEntityAtIndex(player, player.chunkCoordY);
        }
    }

    @Redirect(method = "changePlayerDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;removeEntityDangerously(Lnet/minecraft/entity/Entity;)V"))
    private void removePlayerOnDimensionChange(WorldServer world, Entity player) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            world.removeEntity(player);
        } else {
            world.removeEntityDangerously(player);
        }
    }

    @Inject(method = "transferEntityToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0))
    private void onTransfer(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, CallbackInfo ci) {
        // Players pulling disappear act when using portals. Fix for MC-92916 CARPET-XCOM
        if (CarpetSettings.playersTurningInvisibleFix && entityIn.addedToChunk && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
            if (entityIn.addedToChunk && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
                oldWorldIn.getChunk(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }
        }
    }
}
