package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class RedstoneOreRedirectHelper
{
    private final BlockRedstoneWire wire;
    
    public RedstoneOreRedirectHelper(BlockRedstoneWire redstoneWire)
    {
        this.wire = redstoneWire;
    }
    
    public static boolean canConnectToCM(IBlockState blockState, @Nullable EnumFacing side)
    {
        Block block = blockState.getBlock();
        
        if (block == Blocks.REDSTONE_WIRE)
        {
            return true;
        }
        else if (Blocks.UNPOWERED_REPEATER.isSameDiode(blockState))
        {
            EnumFacing enumfacing = (EnumFacing)blockState.getValue(BlockRedstoneRepeater.FACING);
            return enumfacing == side || enumfacing.getOpposite() == side;
        }
        else if (Blocks.OBSERVER == blockState.getBlock())
        {
            return side == blockState.getValue(BlockObserver.FACING);
        }
        else
        {
            // return blockState.canProvidePower() && side != null;
            return (blockState.canProvidePower() || block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) && side != null;
        }
    }
    
    public int getWeakPowerCM(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side.getOpposite()));
        if (!wire.canProvidePower)
        {
            return 0;
        }
        else
        {
            int i = ((Integer)blockState.getValue(wire.POWER)).intValue();
            
            if (i == 0)
            {
                return 0;
            }
            else if (side == EnumFacing.UP)
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
                EnumSet<EnumFacing> enumset = EnumSet.<EnumFacing>noneOf(EnumFacing.class);
                
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                {
                    if (wire.isPowerSourceAt(blockAccess, pos, enumfacing))
                    {
                        enumset.add(enumfacing);
                    }
                }
                
                if (side.getAxis().isHorizontal() && enumset.isEmpty())
                {
                    return i;
                }
                else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY()))
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
