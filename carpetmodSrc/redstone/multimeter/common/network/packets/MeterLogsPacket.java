package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class MeterLogsPacket implements RSMMPacket {
	
	private NBTTagCompound logsData;
	
	public MeterLogsPacket() {
		
	}
	
	public MeterLogsPacket(NBTTagCompound data) {
		this.logsData = data;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setTag("logs", logsData);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		logsData = data.getCompoundTag("logs");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		
	}
}
