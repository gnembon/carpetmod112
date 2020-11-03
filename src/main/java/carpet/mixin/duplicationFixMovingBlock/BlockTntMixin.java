package carpet.mixin.duplicationFixMovingBlock;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockTNT.class)
public class BlockTntMixin {
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void fixDupe(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.duplicationFixMovingTNT && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
