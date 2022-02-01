package redstone.multimeter.interfaces;

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
}
