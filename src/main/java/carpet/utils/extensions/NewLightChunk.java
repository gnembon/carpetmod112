package carpet.utils.extensions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public interface NewLightChunk {
    short[] getNeighborLightChecks();
    void setNeighborLightChecks(short[] checks);
    short getPendingNeighborLightInits();
    void setPendingNeighborLightInits(int inits);
    int getCachedLightFor(LightType type, BlockPos pos);
}
