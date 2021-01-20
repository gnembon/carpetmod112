package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlock.class)
public class FurnaceBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void noDefaultDirectionOnAdd(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        // A bug that causes bad rotations fixed in 1.13 CARPET-XCOM -> not needed in 1.13
        if (CarpetSettings.accurateBlockPlacement) ci.cancel();
    }
}
