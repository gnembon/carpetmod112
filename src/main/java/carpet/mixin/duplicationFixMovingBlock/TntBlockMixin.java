package carpet.mixin.duplicationFixMovingBlock;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void fixDupe(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.duplicationFixMovingTNT && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
