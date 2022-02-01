package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class RemoveMeterPacket implements RSMMPacket {
	
	private long id;
	
	public RemoveMeterPacket() {
		
	}
	
	public RemoveMeterPacket(long id) {
		this.id = id;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setLong("id", id);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		id = data.getLong("id");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		server.getMultimeter().removeMeter(player, id);
	}
}
