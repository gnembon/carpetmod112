package carpet.utils.extensions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

public interface NewLightChunk {
    short[] getNeighborLightChecks();
    void setNeighborLightChecks(short[] checks);
    short getPendingNeighborLightInits();
    void setPendingNeighborLightInits(int inits);
    int getCachedLightFor(EnumSkyBlock type, BlockPos pos);
}
