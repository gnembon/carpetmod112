package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class MeterUpdatePacket implements RSMMPacket {
	
	private long id;
	private MeterProperties properties;
	
	public MeterUpdatePacket() {
		
	}
	
	public MeterUpdatePacket(long id, MeterProperties properties) {
		this.id = id;
		this.properties = properties;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setLong("id", id);
		data.setTag("properties", properties.toNbt());
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		id = data.getLong("id");
		properties = MeterProperties.fromNbt(data.getCompoundTag("properties"));
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		server.getMultimeter().updateMeter(player, id, properties);
	}
}
