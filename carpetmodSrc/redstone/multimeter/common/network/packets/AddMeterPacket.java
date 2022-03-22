package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class AddMeterPacket implements RSMMPacket {
	
	private MeterProperties properties;
	
	public AddMeterPacket() {
		
	}
	
	public AddMeterPacket(MeterProperties properties) {
		this.properties = properties;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setTag("properties", properties.toNbt());
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		properties = MeterProperties.fromNbt(data.getCompoundTag("properties"));
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		server.getMultimeter().addMeter(player, properties);
	}
}
