package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class ServerTickPacket implements RSMMPacket {
	
	private long serverTime;
	
	public ServerTickPacket() {
		
	}
	
	public ServerTickPacket(long serverTime) {
		this.serverTime = serverTime;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setLong("server time", serverTime);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		serverTime = data.getLong("server time");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		
	}
}
