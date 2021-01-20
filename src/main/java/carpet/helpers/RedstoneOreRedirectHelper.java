package carpet.helpers;

import carpet.mixin.accessors.RedstoneWireBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class RedstoneOreRedirectHelper
{
    
    public static boolean canConnectToCM(BlockState blockState, @Nullable Direction side)
    {
        Block block = blockState.getBlock();
        
        if (block == Blocks.REDSTONE_WIRE)
        {
            return true;
        }
        else if (Blocks.UNPOWERED_REPEATER.method_26548(blockState))
        {
            Direction enumfacing = (Direction)blockState.get(RepeaterBlock.field_24496);
            return enumfacing == side || enumfacing.getOpposite() == side;
        }
        else if (Blocks.OBSERVER == blockState.getBlock())
        {
            return side == blockState.get(ObserverBlock.FACING);
        }
        else
        {
            return (blockState.method_27208() || block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) && side != null;
        }
    }
    
    public static int getWeakPowerCM(RedstoneWireBlock wire, BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side)
    {
        BlockState iblockstate = blockAccess.getBlockState(pos.offset(side.getOpposite()));
        if (!((RedstoneWireBlockAccessor) wire).getWiresGivePower())
        {
            return 0;
        }
        else
        {
            int i = blockState.get(RedstoneWireBlock.field_24710);
            
            if (i == 0)
            {
                return 0;
            }
            else if (side == Direction.UP)
            {
                return i;
            }
            // [CM] Redstone ore redirects dust - give power if its redstone ore block
            else if (side.getAxis().isHorizontal() && ((iblockstate.getBlock() == Blocks.REDSTONE_ORE || iblockstate.getBlock() == Blocks.LIT_REDSTONE_ORE)))
            {
                return i;
            }
            else
            {
                EnumSet<Direction> enumset = EnumSet.<Direction>noneOf(Direction.class);
                
                for (Direction enumfacing : Direction.Type.HORIZONTAL)
                {
                    if (((RedstoneWireBlockAccessor) wire).invokeCouldConnectTo(blockAccess, pos, enumfacing))
                    {
                        enumset.add(enumfacing);
                    }
                }
                
                if (side.getAxis().isHorizontal() && enumset.isEmpty())
                {
                    return i;
                }
                else if (enumset.contains(side) && !enumset.contains(side.rotateYCounterclockwise()) && !enumset.contains(side.rotateYClockwise()))
                {
                    return i;
                }
                else
                {
                    return 0;
                }
            }
        }
    }
    
}
