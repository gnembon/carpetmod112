package carpet.utils.portalsearcher;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class PortalSearcherSuperCache extends PortalSearcherAbstract {

    public PortalSearcherSuperCache(World worldIn) {
        super(worldIn);
    }

    @Override
    public PortalSearchResult search(BlockPos searchCenter) {
        long startTimeNanoSec = System.nanoTime();
        BlockPos nearestPortalPos = BlockPos.ORIGIN;
        double d0 = -1.0;

        int maxX = searchCenter.getX() + 128, minX = searchCenter.getX() - 128, maxZ = searchCenter.getZ() + 128, minZ = searchCenter.getZ() - 128;
        for (int cx = (searchCenter.getX() >> 4) - 8; cx <= (searchCenter.getX() >> 4) + 8; ++cx) {
            for (int cz = (searchCenter.getZ() >> 4) - 8; cz <= (searchCenter.getZ() >> 4) + 8; ++cz) {

                SuperCacheHandler handler;
                if (this.worldIn.provider.getDimensionType().getId() == -1) {
                    handler = SuperCacheHandler.getHandlerNether();
                } else {
                    handler = SuperCacheHandler.getHandlerOverworld();
                }
                ChunkPos cPos = new ChunkPos(cx, cz);

                // use vanilla method and check portals
                if (!handler.isMarked(cPos)) {
                    handler.markChunk(cPos);
                    for (int dx = 0; dx < 16; ++dx) {
                        for (int dz = 0; dz < 16; ++dz) {
                            for (int y = this.worldIn.getActualHeight() - 1; y >= 0; --y) {
                                BlockPos bPos = new BlockPos(
                                        (cx << 4) + dx,
                                        y,
                                        (cz << 4) + dz);
                                if (this.worldIn.getBlockState(bPos).getBlock() == BLOCK_NETHER_PORTAL) {
                                    handler.addPortal(bPos);
                                }
                            }
                        }
                    }
                }

                // look into the portal map
                for (BlockPos portalPos : handler.getChunkPortalIterable(cPos)) {
                    if (portalPos.getX() >= minX
                        && portalPos.getX() <= maxX
                        && portalPos.getZ() >= minZ
                        && portalPos.getZ() <= maxZ) {
                        double d1 = portalPos.distanceSq(searchCenter);
                        if (d0 < 0.0D || d1 < d0 || (d1 == d0 && vallinaComparator.compare(portalPos, nearestPortalPos) < 0)) {
                            d0 = d1;
                            nearestPortalPos = portalPos;
                        }
                    }
                }
            }
        }

        long finishTimeNanoSec = System.nanoTime();
        return new PortalSearchResult(nearestPortalPos, d0, finishTimeNanoSec - startTimeNanoSec);
    }
}
