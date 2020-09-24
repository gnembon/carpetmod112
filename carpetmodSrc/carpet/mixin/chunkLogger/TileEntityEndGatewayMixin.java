package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityEndGateway.class)
public class TileEntityEndGatewayMixin {
    @Inject(method = "findHighestBlock", at = @At("HEAD"))
    private static void onFindHighestBlockStart(World world, BlockPos pos, int range, boolean includeCenter, CallbackInfoReturnable<BlockPos> cir) {
        CarpetClientChunkLogger.setReason("End gateway looking for highest block");
    }

    @Inject(method = "findHighestBlock", at = @At("RETURN"))
    private static void onFindHighestBlockEnd(World world, BlockPos pos, int range, boolean includeCenter, CallbackInfoReturnable<BlockPos> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
