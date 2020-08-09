package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockFurnace.class)
public class BlockFurnaceMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void noDefaultDirectionOnAdd(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        // A bug that causes bad rotations fixed in 1.13 CARPET-XCOM -> not needed in 1.13
        if (CarpetSettings.accurateBlockPlacement) ci.cancel();
    }
}
