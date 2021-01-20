package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "method_27136", at = @At("HEAD"))
    private void onMovementCheck(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason("Piston scheduled by power source");
    }

    @Inject(method = "method_27136", at = @At("RETURN"))
    private void onMovementCheckEnd(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
