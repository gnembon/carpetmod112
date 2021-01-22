package carpet.helpers;
//AUTHOR: PallaPalla
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

// This class contains the code responsible for optimizing tile entities by making them sleep until they receive an update.
// It contains an interface that all optimized tile entities must implement, and the code responsible for propagating the updates.
public class BlockEntityOptimizer
{

    // All optimized tile entities must implement this interface so that the world object knows it should wake them up.
    // A tile entity that implements this interface should set a sleeping flag if it becomes unused.
    // This sleeping flag should causes it to skip all or part of its update() method and be reset by wakeUp().
    public interface LazyBlockEntity
    {
        /**
         * CARPET-optimizedTileEntities: Wakes up the tile entity so it updates again. Called upon receiving a comparator update in
         * {@linkplain net.minecraft.world.World#updateComparatorOutputLevel(net.minecraft.util.math.BlockPos, net.minecraft.block.Block)}
         */
        public void wakeUp();
    }

    // The method called by the world object when a comparator update happens. Wakes up the tile entity causing it, and nearby hoppers.
    // Some code here is copied from world.updateComparatorOutputLevel() to perform the vanilla comparator updates.
    public static void updateComparatorsAndLazyTileEntities(World worldIn, BlockPos pos, Block blockIn)
    {
        // Wake up the tile entity that caused the comparator update
        if (blockIn.hasBlockEntity())
        {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if(tileEntity instanceof LazyBlockEntity)
            {
                ((LazyBlockEntity) tileEntity).wakeUp();
            }
        }

        // Perform the usual comparator updates horizontally
        // Additionally iterate over the up and down directions, since hoppers can also be vertical
        for (Direction enumfacing : Direction.values())
        {
            BlockPos blockpos = pos.offset(enumfacing);
            boolean horizontal = enumfacing.getAxis() != Axis.Y;

            if (worldIn.canSetBlock(blockpos))
            {
                BlockState iblockstate = worldIn.getBlockState(blockpos);
                
                // Check for comparators like in vanilla. This check is only performed horizontally, as comparators are only horizontal
                if (horizontal && Blocks.UNPOWERED_COMPARATOR.method_26548(iblockstate))
                {
                    iblockstate.neighbourUpdate(worldIn, blockpos, blockIn, pos);
                }
                else if (horizontal && iblockstate.isSolidBlock())
                {
                    blockpos = blockpos.offset(enumfacing);
                    iblockstate = worldIn.getBlockState(blockpos);

                    if (Blocks.UNPOWERED_COMPARATOR.method_26548(iblockstate))
                    {
                        iblockstate.neighbourUpdate(worldIn, blockpos, blockIn, pos);
                    }
                }
                
                // Wake up nearby hoppers. Only hoppers under the block (pulling) or pointing into it (pushing) should be woken up
                else if (iblockstate.getBlock() == Blocks.HOPPER)
                {
                    BlockEntity blockEntity = worldIn.getBlockEntity(blockpos);
                    if((enumfacing == Direction.DOWN || enumfacing == HopperBlock.method_26653(blockEntity.method_26938()).getOpposite())
                            && blockEntity instanceof LazyBlockEntity)
                    {
                        ((LazyBlockEntity) blockEntity).wakeUp();
                    }
                }
            }
        }
    }
}
