package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public class MeterGroupRefreshPacket implements RSMMPacket {
	
	private String name;
	private NBTTagCompound meterGroupData;
	
	public MeterGroupRefreshPacket() {
		
	}
	
	public MeterGroupRefreshPacket(MeterGroup meterGroup) {
		this.name = meterGroup.getName();
		this.meterGroupData = meterGroup.toNbt();
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setString("name", name);
		data.setTag("data", meterGroupData);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		name = data.getString("name");
		meterGroupData = data.getCompoundTag("data");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		Multimeter multimeter = server.getMultimeter();
		
		if (multimeter.hasSubscription(player)) {
			multimeter.refreshMeterGroup(player);
		}
	}
}
