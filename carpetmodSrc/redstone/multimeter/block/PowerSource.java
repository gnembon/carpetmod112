package redstone.multimeter.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import redstone.multimeter.interfaces.IBlock;

public interface PowerSource extends IBlock {
	
	public static final int MIN_POWER = 0;
	public static final int MAX_POWER = 15;
	
	@Override
	default boolean isPowerSource() {
		return true;
	}
	
	default boolean logPowerChangeOnStateChange() {
		return true;
	}
	
	public int getPowerLevel(World world, BlockPos pos, IBlockState state);
	
}
