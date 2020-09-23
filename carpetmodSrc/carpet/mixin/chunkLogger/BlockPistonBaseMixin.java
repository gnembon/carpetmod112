package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Inject(method = "checkForMove", at = @At("HEAD"))
    private void onMovementCheck(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason("Piston scheduled by power source");
    }

    @Inject(method = "checkForMove", at = @At("RETURN"))
    private void onMovementCheckEnd(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
