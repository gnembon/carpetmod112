package redstone.multimeter.common.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;
import redstone.multimeter.util.NbtUtils;

public class MeterUpdatesPacket implements RSMMPacket {
	
	private List<Long> removedMeters;
	private Long2ObjectMap<MeterProperties> meterUpdates;
	
	public MeterUpdatesPacket() {
		this.removedMeters = new ArrayList<>();
		this.meterUpdates = new Long2ObjectOpenHashMap<>();
	}
	
	public MeterUpdatesPacket(List<Long> removedMeters, Map<Long, MeterProperties> updates) {
		this.removedMeters = new ArrayList<>(removedMeters);
		this.meterUpdates = new Long2ObjectOpenHashMap<>(updates);
	}
	
	@Override
	public void encode(NBTTagCompound data) {
		NBTTagList ids = new NBTTagList();
		NBTTagList list = new NBTTagList();
		
		for (int index = 0; index < removedMeters.size(); index++) {
			long id = removedMeters.get(index);
			
			NBTTagLong nbt = new NBTTagLong(id);
			ids.appendTag(nbt);
		}
		for (Entry<MeterProperties> entry : meterUpdates.long2ObjectEntrySet()) {
			long id = entry.getLongKey();
			MeterProperties update = entry.getValue();
			
			NBTTagCompound nbt = update.toNbt();
			nbt.setLong("id", id);
			list.appendTag(nbt);
		}
		
		data.setTag("removed meters", ids);
		data.setTag("meter updates", list);
	}
	
	@Override
	public void decode(NBTTagCompound data) {
		NBTTagList ids = data.getTagList("removed meters", NbtUtils.TYPE_LONG);
		NBTTagList list = data.getTagList("meter updates", NbtUtils.TYPE_COMPOUND);
		
		for (int index = 0; index < ids.tagCount(); index++) {
			NBTBase nbt = ids.get(index);
			
			if (nbt.getId() == NbtUtils.TYPE_LONG) {
				removedMeters.add(((NBTTagLong)nbt).getLong());
			}
		}
		for (int index = 0; index < list.tagCount(); index++) {
			NBTTagCompound nbt = list.getCompoundTagAt(index);
			
			long id = nbt.getLong("id");
			MeterProperties update = MeterProperties.fromNbt(nbt);
			meterUpdates.put(id, update);
		}
	}
	
	@Override
	public void execute(MultimeterServer server, EntityPlayerMP player) {
		
	}
}
