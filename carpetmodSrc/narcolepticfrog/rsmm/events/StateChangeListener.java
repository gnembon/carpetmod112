package narcolepticfrog.rsmm.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface StateChangeListener {

    /**
     * Gets called each time a meter might need to update its measurements on a block.
     */
    void onStateChange(World world, BlockPos pos);

}
