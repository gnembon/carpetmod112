package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
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

@Mixin(ChunkProviderServer.class)
public class ChunkProviderServerMixin {
    @Shadow @Final private WorldServer world;

    @Inject(method = "queueUnload", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private void logUnload(Chunk chunkIn, CallbackInfo ci) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunkIn.x, chunkIn.z, CarpetClientChunkLogger.Event.QUEUE_UNLOAD);
        }
    }

    @Inject(method = "getLoadedChunk", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/Chunk;unloadQueued:Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logCancelUnload(int x, int z, CallbackInfoReturnable<Chunk> cir, long key, Chunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled && chunk.unloadQueued) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.CANCEL_UNLOAD);
        }
    }

    @Inject(method = "loadChunk", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;", remap = false))
    private void logLoad(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.LOADING);
        }
    }

    @Redirect(method = "loadChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V"))
    private void populate(Chunk chunk, IChunkProvider provider, IChunkGenerator generator) {
        try {
            CarpetClientChunkLogger.setReason("Population triggering neighbouring chunks to cancel unload");
            chunk.populate(provider, generator);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Inject(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/IChunkGenerator;generateChunk(II)Lnet/minecraft/world/chunk/Chunk;", shift = At.Shift.AFTER))
    private void logGenerator(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.GENERATING);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void setUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.setReason("Unloading chunk and writing to disk");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logUnload(CallbackInfoReturnable<Boolean> cir, Iterator<Long> iterator, int i, Long key, Chunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.x, chunk.z, CarpetClientChunkLogger.Event.UNLOADING);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;chunkTick()V"))
    private void resetUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
