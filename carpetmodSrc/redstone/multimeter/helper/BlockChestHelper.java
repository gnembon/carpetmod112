package redstone.multimeter.helper;

import carpet.CarpetSettings;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import redstone.multimeter.block.PowerSource;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public class BlockChestHelper {

	public static boolean isTrapped(BlockChest chest) {
		return chest.chestType == BlockChest.Type.TRAP;
	}

	public static boolean isTrapped(TileEntityChest chest) {
		return chest.getChestType() == BlockChest.Type.TRAP;
	}

	public static int getPower(World world, BlockPos pos, IBlockState state) {
    	TileEntity blockEntity = world.getTileEntity(pos);

        if (blockEntity instanceof TileEntityChest) {
        	return getPowerFromViewerCount(((TileEntityChest)blockEntity).numPlayersUsing);
        }

        return PowerSource.MIN_POWER;
	}

	public static int getPowerFromViewerCount(int viewerCount) {
		return MathHelper.clamp(viewerCount, PowerSource.MIN_POWER, PowerSource.MAX_POWER);
	}

	public static void onInvOpenOrClosed(TileEntityChest chest, boolean open) {
		if (CarpetSettings.redstoneMultimeter && isTrapped(chest)) {
			WorldServer world = (WorldServer)chest.getWorld();
			BlockPos pos = chest.getPos();

			MultimeterServer server = WorldHelper.getMultimeterServer();
			Multimeter multimeter = server.getMultimeter();

			int viewerCount = chest.numPlayersUsing;
			int oldViewerCount = open ? viewerCount - 1 : viewerCount + 1;

			int oldPower = BlockChestHelper.getPowerFromViewerCount(oldViewerCount);
			int newPower = BlockChestHelper.getPowerFromViewerCount(viewerCount);

			multimeter.logPowerChange(world, pos, oldPower, newPower);
			multimeter.logActive(world, pos, newPower > PowerSource.MIN_POWER);
        }
    }
}
