package carpet.utils.portalsearcher;

import net.minecraft.block.BlockPortal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Comparator;

public abstract class PortalSearcherAbstract {
    protected final World worldIn;
    public static BlockPortal BLOCK_NETHER_PORTAL = (BlockPortal) Blocks.PORTAL;
    protected static final Comparator<BlockPos> vallinaComparator =
            Comparator.comparing(BlockPos::getX).thenComparing(BlockPos::getZ).thenComparing(p -> -p.getY());

    public PortalSearcherAbstract(World worldIn) {
        this.worldIn = worldIn;
    }

    public abstract PortalSearchResult search(BlockPos searchCenter);
}
