package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class)
public class WorldMixin {
    @Inject(method = "updateComparatorOutputLevel", at = @At("HEAD"))
    private void onComparatorUpdate(BlockPos pos, Block blockIn, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason("Comparator updates for inventory changes");
    }

    @Inject(method = "updateComparatorOutputLevel", at = @At("RETURN"))
    private void onComparatorUpdateEnd(BlockPos pos, Block blockIn, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    private void logOnRemoveEntity(Chunk chunk, Entity entity) {
        CarpetClientChunkLogger.setReason(() -> "Removing entity from chunk: " + entity.getName());
    }
}
