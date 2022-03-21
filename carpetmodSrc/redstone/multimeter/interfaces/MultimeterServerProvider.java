package redstone.multimeter.interfaces;

import carpet.CarpetServer;
import carpet.CarpetSettings;

import redstone.multimeter.common.TickTask;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public interface MultimeterServerProvider extends TickTaskExecutor {
	
	@Override
	default void startTickTask(boolean updateTree, TickTask task, String... args) {
		if (CarpetSettings.redstoneMultimeter) {
		    getMultimeterServer().startTickTask(updateTree, task, args);
		}
	}
	
	@Override
	default void endTickTask(boolean updateTree) {
		if (CarpetSettings.redstoneMultimeter) {
		    getMultimeterServer().endTickTask(updateTree);
		}
	}
	
	@Override
	default void swapTickTask(boolean updateTree, TickTask task, String... args) {
		if (CarpetSettings.redstoneMultimeter) {
		    getMultimeterServer().swapTickTask(updateTree, task, args);
		}
	}
	
	default MultimeterServer getMultimeterServer() {
		return CarpetServer.rsmmServer;
	}
	
	default Multimeter getMultimeter() {
		return CarpetServer.rsmmServer.getMultimeter();
	}
}
