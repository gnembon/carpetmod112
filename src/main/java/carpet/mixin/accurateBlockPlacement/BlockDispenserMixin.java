package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockDispenser.class)
public class BlockDispenserMixin {
    @Inject(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockDispenser;setDefaultDirection(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"), cancellable = true)
    private void noDefaultDirectionOnAdd(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        // A bug that causes bad rotations fixed in 1.13 CARPET-XCOM -> not needed in 1.13
        if (CarpetSettings.accurateBlockPlacement) ci.cancel();
    }

    @Redirect(method = "onBlockPlacedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean noSetBlockState(World world, BlockPos pos, IBlockState newState, int flags) {
        if (CarpetSettings.accurateBlockPlacement) return false;
        return world.setBlockState(pos, newState, flags);
    }
}
