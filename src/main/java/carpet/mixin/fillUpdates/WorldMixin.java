package carpet.mixin.fillUpdates;

import carpet.utils.extensions.ExtendedChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState setBlockStateCarpet(Chunk chunk, BlockPos pos, IBlockState state, BlockPos posAgain, IBlockState newStateAgain, int flags) {
        return ((ExtendedChunk) chunk).setBlockStateCarpet(pos, state, (flags & 128) != 0);
    }

    @ModifyConstant(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", constant = @Constant(intValue = 16))
    private int checkNoUpdateFlag(int flags) {
        return flags | 128;
    }
}
