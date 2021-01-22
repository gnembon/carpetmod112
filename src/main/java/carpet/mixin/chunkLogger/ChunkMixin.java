package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;

    @Inject(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;markDirty()V", ordinal = 0))
    private void onPopulateStructures(ChunkGenerator generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.GENERATING_STRUCTURES);
    }

    @Inject(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkGenerator;decorate(II)V"))
    private void onPopulate(ChunkGenerator generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.POPULATING);
    }
}
