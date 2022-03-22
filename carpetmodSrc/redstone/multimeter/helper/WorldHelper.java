package redstone.multimeter.helper;

import carpet.CarpetServer;
import carpet.CarpetSettings;

import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;

import redstone.multimeter.common.TickTask;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public class WorldHelper {
	
	public static int currentBlockEventDepth;
	
	public static MultimeterServer getMultimeterServer() {
		return CarpetServer.rsmmServer;
	}
	
	public static Multimeter getMultimeter() {
		return CarpetServer.rsmmServer.getMultimeter();
	}
	
	public static void startTickTask(TickTask task, String... args) {
		startTickTask(true, task, args);
	}
	
	public static void startTickTask(boolean updateTree, TickTask task, String... args) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeterServer().startTickTask(updateTree, task, args);
		}
	}
	
	public static void endTickTask() {
		endTickTask(true);
	}
	
	public static void endTickTask(boolean updateTree) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeterServer().endTickTask(updateTree);
		}
	}
	
	public static void swapTickTask(TickTask task, String... args) {
		swapTickTask(true, task, args);
	}
	
	public static void swapTickTask(boolean updateTree, TickTask task, String... args) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeterServer().swapTickTask(updateTree, task, args);
		}
	}
	
	public static void onBlockUpdate(World world, BlockPos pos, IBlockState state) {
		if (CarpetSettings.redstoneMultimeter) {
			MultimeterServer server = getMultimeterServer();
			Multimeter multimeter = server.getMultimeter();

			multimeter.logBlockUpdate(world, pos);

			if (state.getBlock().logPoweredOnBlockUpdate()) {
				multimeter.logPowered(world, pos, state);
			}
		}
	}
	
	public static void onObserverUpdate(World world, BlockPos pos) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeter().logObserverUpdate(world, pos);
		}
	}
	
	public static void onEntityTick(World world, Entity entity) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeter().logEntityTick(world, entity);
		}
	}
	
	public static void onBlockEntityTick(World world, TileEntity blockEntity) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeter().logBlockEntityTick(world, blockEntity);
		}
	}
	
	public static void onComparatorUpdate(World world, BlockPos pos) {
		if (CarpetSettings.redstoneMultimeter) {
			getMultimeter().logComparatorUpdate(world, pos);
		}
	}
	
	public static void onRandomTick(World world, BlockPos pos) {
        if (CarpetSettings.redstoneMultimeter) {
            getMultimeter().logRandomTick(world, pos);
        }
    }
	
	public static void onScheduledTick(World world, NextTickListEntry scheduledTick) {
        if (CarpetSettings.redstoneMultimeter) {
            getMultimeter().logScheduledTick(world, scheduledTick);
        }
    }
	
	public static void onBlockEvent(World world, BlockEventData blockEvent) {
        if (CarpetSettings.redstoneMultimeter) {
            getMultimeter().logBlockEvent(world, blockEvent, currentBlockEventDepth);
        }
    }
}
