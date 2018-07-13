package narcolepticfrog.rsmm.events;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PistonPushListener {

    /**
     * Gets called each time a block is moved by a piston. `pos` refers to the blocks
     * position before being moved.
     */
    void onPistonPush(World w, BlockPos pos, EnumFacing direction);

}
