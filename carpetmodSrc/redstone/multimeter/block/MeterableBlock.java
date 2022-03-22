package redstone.multimeter.block;

import carpet.CarpetSettings;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import redstone.multimeter.helper.WorldHelper;

public interface MeterableBlock extends Meterable {
	
	default void logPowered(World world, BlockPos pos, boolean powered) {
		if (CarpetSettings.redstoneMultimeter && !world.isRemote) {
			WorldHelper.getMultimeter().logPowered(world, pos, powered);
		}
	}
	
	default void logPowered(World world, BlockPos pos, IBlockState state) {
		if (CarpetSettings.redstoneMultimeter && !world.isRemote) {
			WorldHelper.getMultimeter().logPowered(world, pos, state);
		}
	}
}
