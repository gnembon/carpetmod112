package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndGatewayBlockEntity.class)
public class EndGatewayBlockEntityMixin {
    @Inject(method = "findExitPortalPos", at = @At("HEAD"))
    private static void onFindExitPortalPosStart(World world, BlockPos pos, int range, boolean includeCenter, CallbackInfoReturnable<BlockPos> cir) {
        CarpetClientChunkLogger.setReason("End gateway looking for highest block");
    }

    @Inject(method = "findExitPortalPos", at = @At("RETURN"))
    private static void onFindExitPortalPosEnd(World world, BlockPos pos, int range, boolean includeCenter, CallbackInfoReturnable<BlockPos> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
