package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Inject(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, Iterator<NextTickListEntry> iterator, NextTickListEntry entry, int unused) {
        CarpetClientChunkLogger.setReason(() -> "Block update: " + Block.REGISTRY.getNameForObject(entry.getBlock()) + " at " + entry.position);
    }

    // extra int i
    @Surrogate
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, int listSize, Iterator<NextTickListEntry> iterator, NextTickListEntry entry) {
        setChunkLoadingReason(runAllPending, cir, iterator, entry, 0);
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void resetChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }

    @Inject(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBlockEvent(CallbackInfo ci, int index, Iterator<BlockEventData> iterator, BlockEventData data) {
        CarpetClientChunkLogger.setReason(() -> "Queued block event: " + data);
    }

    @Inject(method = "sendQueuedBlockEvents", at = @At("RETURN"))
    private void onBlockEventsDone(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
