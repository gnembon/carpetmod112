package carpet.mixin.duplicationFixMovingRail;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRailBase.class)
public class BlockRailBaseMixin {
    @Inject(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRailBase;dropBlockAsItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V"), cancellable = true)
    private void fixDupe(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if(CarpetSettings.duplicationFixMovingRail && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
