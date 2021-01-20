package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class ChunkMixin {
    @Shadow @Final private World world;
    @Shadow @Final public int field_25365;
    @Shadow @Final public int field_25366;

    @Inject(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;markDirty()V", ordinal = 0))
    private void onPopulateStructures(ChunkManager generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, field_25365, field_25366, CarpetClientChunkLogger.Event.GENERATING_STRUCTURES);
    }

    @Inject(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkManager;method_27344(II)V"))
    private void onPopulate(ChunkManager generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, field_25365, field_25366, CarpetClientChunkLogger.Event.POPULATING);
    }
}
