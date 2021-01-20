package carpet.helpers;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;

public class ScheduledTickFix<T> extends ScheduledTick {
    public ScheduledTickFix(BlockPos positionIn, Block blockIn) {
        super(positionIn, blockIn);
    }
}
