package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ServerWorld.class)
public class WorldServerMixin {
    @Inject(method = "method_26051", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, Iterator<ScheduledTick> iterator, ScheduledTick entry, int unused) {
        CarpetClientChunkLogger.setReason(() -> "Block update: " + Block.REGISTRY.getId(entry.method_26224()) + " at " + entry.pos);
    }

    /*
    // extra int i
    @Surrogate
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, int listSize, Iterator<NextTickListEntry> iterator, NextTickListEntry entry) {
        setChunkLoadingReason(runAllPending, cir, iterator, entry, 0);
    }
     */

    @Inject(method = "method_26051", at = @At("RETURN"))
    private void resetChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }

    @Inject(method = "sendBlockActions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_33691(Lnet/minecraft/entity/player/PlayerEntity;DDDDILnet/minecraft/network/Packet;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBlockEvent(CallbackInfo ci, int index, Iterator<BlockAction> iterator, BlockAction data) {
        CarpetClientChunkLogger.setReason(() -> "Queued block event: " + data);
    }

    @Inject(method = "sendBlockActions", at = @At("RETURN"))
    private void onBlockEventsDone(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
