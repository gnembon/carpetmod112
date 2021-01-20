package carpet.helpers;

import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSaplingHelper
{
    // Added code for checking water for dead shrub rule CARPET-XCOM
    public static boolean hasWater(World worldIn, BlockPos pos)
    {
        for (BlockPos.Mutable blockpos$mutableblockpos : BlockPos.method_31901(pos.add(-4, -4, -4), pos.add(4, 1, 4)))
        {
            if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER)
            {
                return true;
            }
        }

        return false;
    }
}
