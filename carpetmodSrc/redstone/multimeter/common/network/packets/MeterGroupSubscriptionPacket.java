package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class MeterGroupSubscriptionPacket implements RSMMPacket {
	
	private String name;
	private boolean subscribed;
	
	public MeterGroupSubscriptionPacket() {
		
	}
	
	public MeterGroupSubscriptionPacket(String name, boolean subscribed) {
		this.name = name;
		this.subscribed = subscribed;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setString("name", name);
		data.setBoolean("subscribed", subscribed);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		name = data.getString("name");
		subscribed = data.getBoolean("subscribed");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		Multimeter multimeter = server.getMultimeter();
		ServerMeterGroup meterGroup = multimeter.getMeterGroup(name);
		
		if (subscribed) {
			if (meterGroup == null) {
				multimeter.createMeterGroup(player, name);
			} else {
				multimeter.subscribeToMeterGroup(meterGroup, player);
			}
		} else {
			if (meterGroup == null) {
				multimeter.refreshMeterGroup(player);
			} else {
				multimeter.unsubscribeFromMeterGroup(meterGroup, player);
			}
		}
	}
}
