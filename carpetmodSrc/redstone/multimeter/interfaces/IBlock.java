package redstone.multimeter.interfaces;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlock {
	
	default boolean isMeterable() {
		return false;
	}
	
	default boolean isPowerSource() {
		return false;
	}
	
	default boolean logPoweredOnBlockUpdate() {
		return true;
	}
	
	default boolean isPowered(World world, BlockPos pos, IBlockState state) {
		return world.isBlockPowered(pos);
	}
}
