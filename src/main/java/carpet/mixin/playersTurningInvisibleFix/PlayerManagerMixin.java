package carpet.mixin.playersTurningInvisibleFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.WorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
    private void removeFromChunk(ServerPlayerEntity player, int dimension, boolean conqueredEnd, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            player.getServerWorld().method_25975(player.chunkX, player.chunkZ).remove(player, player.chunkY);
        }
    }

    @Redirect(method = "method_33705", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_26123(Lnet/minecraft/entity/Entity;)V"))
    private void removePlayerOnDimensionChange(ServerWorld world, Entity player) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            world.method_26119(player);
        } else {
            world.method_26123(player);
        }
    }

    @Inject(method = "method_33709", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0))
    private void onTransfer(Entity entityIn, int lastDimension, ServerWorld oldWorldIn, ServerWorld toWorldIn, CallbackInfo ci) {
        // Players pulling disappear act when using portals. Fix for MC-92916 CARPET-XCOM
        if (CarpetSettings.playersTurningInvisibleFix && entityIn.updateNeeded && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkX, entityIn.chunkZ, true)) {
            if (entityIn.updateNeeded && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkX, entityIn.chunkZ, true)) {
                oldWorldIn.method_25975(entityIn.chunkX, entityIn.chunkZ).remove(entityIn, entityIn.chunkY);
            }
        }
    }
}
