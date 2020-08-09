package carpet.mixin.duplicationFixMovingCarpets;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockCarpet.class)
public class BlockCarpetMixin {
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void fixDupe(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.duplicationFixMovingCarpets && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
