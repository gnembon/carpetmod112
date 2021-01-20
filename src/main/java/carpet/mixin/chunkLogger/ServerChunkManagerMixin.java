package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import net.minecraft.class_5305;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow @Final private ServerWorld world;

    @Inject(method = "method_33448", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private void logUnload(WorldChunk chunk, CallbackInfo ci) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.field_25365, chunk.field_25365, CarpetClientChunkLogger.Event.QUEUE_UNLOAD);
        }
    }

    @Inject(method = "method_27346", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/WorldChunk;field_25367:Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logCancelUnload(int x, int z, CallbackInfoReturnable<WorldChunk> cir, long key, WorldChunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled && chunk.field_25367) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.CANCEL_UNLOAD);
        }
    }

    @Inject(method = "method_33452", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;", remap = false))
    private void logLoad(int x, int z, CallbackInfoReturnable<WorldChunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.LOADING);
        }
    }

    @Redirect(method = "method_33452", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;method_27368(Lnet/minecraft/class_5305;Lnet/minecraft/world/chunk/ChunkManager;)V"))
    private void populate(WorldChunk chunk, class_5305 provider, ChunkManager generator) {
        try {
            CarpetClientChunkLogger.setReason("Population triggering neighbouring chunks to cancel unload");
            chunk.method_27368(provider, generator);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Inject(method = "method_27347", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkManager;method_27339(II)Lnet/minecraft/world/chunk/WorldChunk;", shift = At.Shift.AFTER))
    private void logGenerator(int x, int z, CallbackInfoReturnable<WorldChunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.GENERATING);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void setUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.setReason("Unloading chunk and writing to disk");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logUnload(CallbackInfoReturnable<Boolean> cir, Iterator<Long> iterator, int i, Long key, WorldChunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.field_25365, chunk.field_25366, CarpetClientChunkLogger.Event.UNLOADING);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5318;method_27478()V"))
    private void resetUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
