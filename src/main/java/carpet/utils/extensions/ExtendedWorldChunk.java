package carpet.utils.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorldChunk {
    BlockState setBlockStateCarpet(BlockPos pos, BlockState state, boolean skipUpdates);
}
