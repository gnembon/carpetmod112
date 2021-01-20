package carpet.mixin.fillUpdates;

import carpet.utils.extensions.ExtendedWorldChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;method_27373(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"))
    private BlockState setBlockStateCarpet(WorldChunk chunk, BlockPos pos, BlockState state, BlockPos posAgain, BlockState newStateAgain, int flags) {
        return ((ExtendedWorldChunk) chunk).setBlockStateCarpet(pos, state, (flags & 128) != 0);
    }

    @ModifyConstant(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", constant = @Constant(intValue = 16))
    private int checkNoUpdateFlag(int flags) {
        return flags | 128;
    }
}
