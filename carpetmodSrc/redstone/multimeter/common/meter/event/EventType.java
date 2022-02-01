package redstone.multimeter.common.meter.event;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;

import redstone.multimeter.util.NbtUtils;

public enum EventType {
	
	UNKNOWN(-1, "unknown"),
	POWERED(0, "powered"),
	ACTIVE(1, "active"),
	MOVED(2, "moved"),
	POWER_CHANGE(3, "power_change"),
	RANDOM_TICK(4, "random_tick"),
	SCHEDULED_TICK(5, "scheduled_tick"),
	BLOCK_EVENT(6, "block_event"),
	ENTITY_TICK(7, "entity_tick"),
	BLOCK_ENTITY_TICK(8, "block_entity_tick"),
	BLOCK_UPDATE(9, "block_update"),
	COMPARATOR_UPDATE(10, "comparator_update"),
	SHAPE_UPDATE(11, "shape_update"),
	OBSERVER_UPDATE(12, "observer_update"),
	INTERACT_BLOCK(13, "interact_block");
	
	public static final EventType[] ALL;
	private static final Map<String, EventType> BY_NAME;
	
	static {
		EventType[] types = values();
		
		ALL = new EventType[types.length - 1];
		BY_NAME = new HashMap<>();
		
		for (int index = 1; index < types.length; index++) {
			EventType type = types[index];
			
			ALL[type.index] = type;
			BY_NAME.put(type.name, type);
		}
	}
	
	private final int index;
	private final String name;
	
	private EventType(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static EventType fromIndex(int index) {
		if (index >= 0 && index < ALL.length) {
			return ALL[index];
		}
		
		return UNKNOWN;
	}
	
	public String getName() {
		return name;
	}
	
	public static EventType fromName(String name) {
		return BY_NAME.getOrDefault(name, UNKNOWN);
	}
	
	public int flag() {
		return 1 << index;
	}
	
	public NBTBase toNbt() {
		return new NBTTagByte((byte)index);
	}
	
	public static EventType fromNbt(NBTBase nbt) {
		if (nbt.getId() != NbtUtils.TYPE_BYTE) {
			return UNKNOWN;
		}
		
		NBTTagByte NBTTagByte = (NBTTagByte)nbt;
		int index = NBTTagByte.getByte();
		
		return fromIndex(index);
	}
}
