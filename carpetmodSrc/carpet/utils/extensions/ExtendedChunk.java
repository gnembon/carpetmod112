package carpet.utils.extensions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface ExtendedChunk {
    IBlockState setBlockStateCarpet(BlockPos pos, IBlockState state, boolean skipUpdates);
}
