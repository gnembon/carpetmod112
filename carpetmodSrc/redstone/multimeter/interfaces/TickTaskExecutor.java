package redstone.multimeter.interfaces;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import redstone.multimeter.common.TickTask;

public interface TickTaskExecutor {
	
	default void startTickTask(TickTask task, String... args) {
		startTickTask(true, task, args);
	}
	
	default void startTickTask(boolean updateTree, TickTask task, String... args) {
		
	}
	
	default void endTickTask() {
		endTickTask(true);
	}
	
	default void endTickTask(boolean updateTree) {
		
	}
	
	default void swapTickTask(TickTask task, String... args) {
		swapTickTask(true, task, args);
	}
	
	default void swapTickTask(boolean updateTree, TickTask task, String... args) {
		
	}
	
	default void onBlockUpdate(BlockPos pos, IBlockState state) {
		
	}
	
	default void onObserverUpdate(BlockPos pos) {
		
	}
	
	default void onEntityTick(Entity entity) {
		
	}
	
	default void onBlockEntityTick(TileEntity blockEntity) {
		
	}
	
	default void onComparatorUpdate(BlockPos pos) {
		
	}
}
