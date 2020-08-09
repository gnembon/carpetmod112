package carpet.mixin.repeaterPoweredTerracotta;

import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRedstoneComparator.class)
public abstract class BlockRedstoneComparatorMixin extends BlockRedstoneDiodeMixin {
    @Shadow protected abstract int getDelay(IBlockState state);

    @Override
    protected int getDelay(IBlockState state, World world, BlockPos pos) {
        return getDelay(state);
    }
}
