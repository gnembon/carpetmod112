package redstone.multimeter.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import redstone.multimeter.interfaces.IBlock;

public interface Meterable extends IBlock {
	
	@Override
	default boolean isMeterable() {
		return true;
	}
	
	public boolean isActive(World world, BlockPos pos, IBlockState state);
	
}
