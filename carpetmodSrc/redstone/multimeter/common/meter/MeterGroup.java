package redstone.multimeter.common.meter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.log.LogManager;
import redstone.multimeter.util.NbtUtils;

public abstract class MeterGroup {
	
	private final String name;
	private final List<Meter> meters;
	private final Map<Long, Integer> idToIndex;
	private final Map<DimPos, Integer> posToIndex;
	
	protected MeterGroup(String name) {
		this.name = name;
		this.meters = new ArrayList<>();
		this.idToIndex = new HashMap<>();
		this.posToIndex = new HashMap<>();
	}
	
	public static boolean isValidName(String name) {
		return !name.trim().isEmpty() && name.length() <= getMaxNameLength();
	}
	
	public static int getMaxNameLength() {
		return 64;
	}
	
	public String getName() {
		return name;
	}
	
	public void clear() {
		meters.clear();
		idToIndex.clear();
		posToIndex.clear();
		getLogManager().clearLogs();
	}
	
	public boolean hasMeters() {
		return !meters.isEmpty();
	}
	
	public List<Meter> getMeters() {
		return Collections.unmodifiableList(meters);
	}
	
	public boolean hasMeter(long id) {
		return idToIndex.containsKey(id);
	}
	
	public boolean hasMeterAt(DimPos pos) {
		return posToIndex.containsKey(pos);
	}
	
	public Meter getMeter(long id) {
		return fromIndex(idToIndex.getOrDefault(id, -1));
	}
	
	public Meter getMeterAt(DimPos pos) {
		return fromIndex(posToIndex.getOrDefault(pos, -1));
	}
	
	private Meter fromIndex(int index) {
		return (index < 0 || index >= meters.size()) ? null : meters.get(index);
	}
	
	protected boolean addMeter(Meter meter) {
		// This check prevents meters from being added twice and
		// multiple meters from being added at the same position.
		if (idToIndex.containsKey(meter.getId()) || posToIndex.containsKey(meter.getPos())) {
			return false;
		}
		
		idToIndex.put(meter.getId(), meters.size());
		posToIndex.put(meter.getPos(), meters.size());
		meters.add(meter);
		
		meterAdded(meter);
		
		return true;
	}
	
	protected boolean removeMeter(Meter meter) {
		int index = idToIndex.getOrDefault(meter.getId(), -1);
		
		if (index < 0 || index >= meters.size()) {
			return false;
		}
		
		meters.remove(index);
		idToIndex.remove(meter.getId(), index);
		posToIndex.remove(meter.getPos(), index);
		
		for (; index < meters.size(); index++) {
			Meter m = meters.get(index);
			
			idToIndex.compute(m.getId(), (id, prevIndex) -> prevIndex - 1);
			posToIndex.compute(m.getPos(), (pos, prevIndex) -> prevIndex - 1);
		}
		
		meterRemoved(meter);
		
		return true;
	}
	
	protected boolean updateMeter(Meter meter, MeterProperties newProperties) {
		meter.applyUpdate(properties -> {
			boolean changed = false;
			
			if (newProperties.getPos() != null) {
				moveMeter(meter, newProperties.getPos());
			}
			if (newProperties.getName() != null) {
				changed |= properties.setName(newProperties.getName());
			}
			if (newProperties.getColor() != null) {
				changed |= properties.setColor(newProperties.getColor());
			}
			if (newProperties.getMovable() != null) {
				changed |= properties.setMovable(newProperties.getMovable());
			}
			if (newProperties.getEventTypes() != null) {
				changed |= properties.setEventTypes(newProperties.getEventTypes());
			}
			
			if (changed) {
				meterUpdated(meter);
			}
		});
		
		return true;
	}
	
	protected void moveMeter(Meter meter, DimPos newPos) {
		long id = meter.getId();
		DimPos pos = meter.getPos();
		
		if (pos.equals(newPos)) {
			return;
		}
		
		int index = idToIndex.getOrDefault(id, -1);
		
		if (index < 0 || index >= meters.size()) {
			return;
		}
		
		posToIndex.remove(pos, index);
		posToIndex.put(newPos, index);
		
		meter.applyUpdate(properties -> {
			if (properties.setPos(newPos)) {
				meterUpdated(meter);
			}
		});
	}
	
	protected abstract void meterAdded(Meter meter);
	
	protected abstract void meterRemoved(Meter meter);
	
	protected abstract void meterUpdated(Meter meter);
	
	public abstract LogManager getLogManager();
	
	public NBTTagCompound toNbt() {
		NBTTagList list = new NBTTagList();
		
		for (Meter meter : meters) {
			list.appendTag(meter.toNbt());
		}
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("meters", list);
		
		return nbt;
	}
	
	public void updateFromNbt(NBTTagCompound nbt) {
		clear();
		
		NBTTagList list = nbt.getTagList("meters", NbtUtils.TYPE_COMPOUND);
		
		for (int index = 0; index < list.tagCount(); index++) {
			NBTTagCompound meterNbt = list.getCompoundTagAt(index);
			Meter meter = Meter.fromNbt(meterNbt);
			
			addMeter(meter);
		}
	}
}
