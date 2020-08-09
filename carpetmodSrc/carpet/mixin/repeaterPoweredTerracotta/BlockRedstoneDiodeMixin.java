package carpet.mixin.repeaterPoweredTerracotta;

import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockRedstoneDiode.class)
public abstract class BlockRedstoneDiodeMixin {
    protected int getTickDelay(IBlockState state, World world, BlockPos pos) {
        return this.getDelay(state, world, pos);
    }

    protected abstract int getDelay(IBlockState state, World world, BlockPos pos);

    @Redirect(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneDiode;getDelay(Lnet/minecraft/block/state/IBlockState;)I"))
    private int getDelay(BlockRedstoneDiode diode, IBlockState state, World world, BlockPos pos) {
        return getDelay(state, world, pos);
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneDiode;getTickDelay(Lnet/minecraft/block/state/IBlockState;)I"))
    private int getTickDelay(BlockRedstoneDiode diode, IBlockState state, World world, BlockPos pos) {
        return getTickDelay(state, world, pos);
    }
}
