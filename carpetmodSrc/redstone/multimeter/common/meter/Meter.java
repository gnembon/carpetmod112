package redstone.multimeter.common.meter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.MeterProperties.MutableMeterProperties;
import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.common.meter.log.MeterLogs;

public class Meter {
	
	private static final AtomicLong ID_COUNTER = new AtomicLong(0);
	
	private final long id;
	private final MutableMeterProperties properties;
	private final MeterLogs logs;
	
	/** true if the block at this position is receiving power */
	private boolean powered;
	/** true if the block at this position is emitting power or active in another way */
	private boolean active;
	
	/** This property is used on the client to hide a meter in the HUD */
	private boolean hidden;
	
	public Meter(long id, MutableMeterProperties properties) {
		this.id = id;
		this.properties = properties.toMutable();
		this.logs = new MeterLogs();
	}
	
	public Meter(MutableMeterProperties properties) {
		this(ID_COUNTER.getAndIncrement(), properties);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Meter) {
			Meter meter = (Meter)obj;
			return meter.id == id;
		}
		
		return false;
	}
	
	public long getId() {
		return id;
	}
	
	public MeterProperties getProperties() {
		return properties.toImmutable();
	}
	
	public MeterLogs getLogs() {
		return logs;
	}
	
	public void applyUpdate(Consumer<MutableMeterProperties> update) {
		update.accept(properties);
	}
	
	public DimPos getPos() {
		return properties.getPos();
	}
	
	public boolean isIn(World world) {
		return properties.getPos().isOf(world);
	}
	
	public String getName() {
		return properties.getName();
	}
	
	public int getColor() {
		return properties.getColor();
	}
	
	public boolean isMovable() {
		return properties.getMovable();
	}
	
	public int getEventTypes() {
		return properties.getEventTypes();
	}
	
	public boolean isMetering(EventType type) {
		return properties.hasEventType(type);
	}
	
	public boolean isPowered() {
		return powered;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean setPowered(boolean powered) {
		boolean wasPowered = this.powered;
		this.powered = powered;
		
		return wasPowered != powered;
	}
	
	public boolean setActive(boolean active) {
		boolean wasActive = this.active;
		this.active = active;
		
		return wasActive != active;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void toggleHidden() {
		setHidden(!hidden);
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public NBTTagCompound toNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setLong("id", id);
		nbt.setTag("properties", properties.toNbt());
		nbt.setBoolean("powered", powered);
		nbt.setBoolean("active", active);
		
		return nbt;
	}
	
	public static Meter fromNbt(NBTTagCompound nbt) {
		long id = nbt.getLong("id");
		MeterProperties properties = MeterProperties.fromNbt(nbt.getCompoundTag("properties"));
		boolean powered = nbt.getBoolean("powered");
		boolean active = nbt.getBoolean("active");
		
		Meter meter = new Meter(id, properties.toMutable());
		meter.setPowered(powered);
		meter.setActive(active);
		
		return meter;
	}
}
