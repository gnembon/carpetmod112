package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Redirect(method = "onBlockPlacedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean accurateSetBlockState(World world, BlockPos pos, IBlockState newState, int flags) {
        return CarpetSettings.accurateBlockPlacement || world.setBlockState(pos, newState, flags);
    }
}
