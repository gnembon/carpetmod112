package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class TickPhaseTreePacket implements RSMMPacket {
	
	private NBTTagCompound nbt;
	
	public TickPhaseTreePacket() {
		
	}
	
	public TickPhaseTreePacket(NBTTagCompound nbt) {
		this.nbt = nbt;
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		data.setTag("tick phase tree", nbt);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		nbt = data.getCompoundTag("tick phase tree");
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		server.refreshTickPhaseTree(player);
	}
}
