package redstone.multimeter.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.server.MultimeterServer;

public interface RSMMPacket {
	
	public void encode(NBTTagCompound data);
	
	public void decode(NBTTagCompound data);
	
	public void execute(MultimeterServer server, EntityPlayerMP player);
	
	/**
	 * Most RSMM packets are ignored if the redstoneMultimeter carpet
	 * rule is not enabled. Some packets are handled anyway in order
	 * to keep RSMM working properly when the carpet rule is toggled.
	 */
	default boolean force() {
		return false;
	}
}
