package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.class_4615;
import net.minecraft.class_6380;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4615.class)
public class PlayerChunkMapEntryMixin {
    @Shadow @Final private class_6380 field_31792;
    @Shadow @Final private ColumnPos field_31794;

    @Inject(method = "method_33566", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
    private void onAdd(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.field_31792.method_33577(), field_31794.x, field_31794.z, CarpetClientChunkLogger.Event.PLAYER_ENTERS);
    }

    @Inject(method = "method_33569", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33589(Lnet/minecraft/class_4615;)V"))
    private void onRemove(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.field_31792.method_33577(), field_31794.x, field_31794.z, CarpetClientChunkLogger.Event.PLAYER_LEAVES);
    }

    @Redirect(method = "method_33567", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkCache;method_27347(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk provideChunk(ServerChunkCache provider, int x, int z) {
        try {
            CarpetClientChunkLogger.setReason("Player loading new chunks and generating");
            return provider.method_27347(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
