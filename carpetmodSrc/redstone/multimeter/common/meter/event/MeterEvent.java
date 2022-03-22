package redstone.multimeter.common.meter.event;

import net.minecraft.nbt.NBTTagCompound;

public class MeterEvent {
	
	private EventType type;
	private int metadata;
	
	private MeterEvent() {
		
	}
	
	public MeterEvent(EventType type, int metadata) {
		this.type = type;
		this.metadata = metadata;
	}
	
	public EventType getType() {
		return type;
	}
	
	public int getMetadata() {
		return metadata;
	}
	
	public NBTTagCompound toNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setTag("type", type.toNbt());
		nbt.setInteger("metadata", metadata);
		
		return nbt;
	}
	
	public static MeterEvent fromNbt(NBTTagCompound nbt) {
		MeterEvent event = new MeterEvent();
		
		event.type = EventType.fromNbt(nbt.getTag("type"));
		event.metadata = nbt.getInteger("metadata");
		
		return event;
	}
}
