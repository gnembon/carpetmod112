package carpet.utils.portalsearcher;

import net.minecraft.util.math.BlockPos;

public class PortalSearchResult {
    private final BlockPos result;
    private final double distance;
    private final long nanoTimeCost;

    public PortalSearchResult(BlockPos result, double distance, long nanoTimeCost) {
        this.result = result;
        this.distance = distance;
        this.nanoTimeCost = nanoTimeCost;
    }

    public double getDistanceSq() {
        return distance;
    }

    public BlockPos getResult() {
        return result;
    }

    public long getNanoTimeCost() {
        return nanoTimeCost;
    }

    @Override
    public String toString() {
        return String.format("Found %s, sqrt(%f) far away within %d nano seconds.", getResult().toString(), getDistanceSq(), getNanoTimeCost());
    }
}
