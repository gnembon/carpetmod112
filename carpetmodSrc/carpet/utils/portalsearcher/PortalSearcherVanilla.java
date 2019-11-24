package carpet.utils.portalsearcher;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalSearcherVanilla extends PortalSearcherAbstract {

    public PortalSearcherVanilla(World worldIn) {
        super(worldIn);
    }

    @Override
    public PortalSearchResult search(BlockPos searchCenter) {
        long startTimeNanoSec = System.nanoTime();

        BlockPos nearestPortalPos = BlockPos.ORIGIN;
        double d0 = -1.0;

        for (int dx = -128; dx <= 128; ++dx) {

            for (int dz = -128; dz <= 128; ++dz) {

                for (BlockPos blockpos2, blockpos1 =
                     searchCenter.add(dx, this.worldIn.getActualHeight() - 1 - searchCenter.getY(), dz);
                     blockpos1.getY() >= 0; blockpos1 = blockpos2) {

                    blockpos2 = blockpos1.down();

                    if (this.worldIn.getBlockState(blockpos1).getBlock() == BLOCK_NETHER_PORTAL) {
                        for (blockpos2 = blockpos1.down();
                             this.worldIn.getBlockState(blockpos2).getBlock() == BLOCK_NETHER_PORTAL;
                             blockpos2 = blockpos2.down()) {

                            blockpos1 = blockpos2;
                        }

                        double d1 = blockpos1.distanceSq(searchCenter);

                        if (d0 < 0.0D || d1 < d0) {
                            d0 = d1;
                            nearestPortalPos = blockpos1;
                        }
                    }
                }
            }
        }
        long finishTimeNanoSec = System.nanoTime();
        return new PortalSearchResult(nearestPortalPos, d0, finishTimeNanoSec - startTimeNanoSec);
    }
}
