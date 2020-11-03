package carpet.helpers;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;

public class NextTickListEntryFix<T> extends NextTickListEntry {
    public NextTickListEntryFix(BlockPos positionIn, Block blockIn) {
        super(positionIn, blockIn);
    }
}
