package redstone.multimeter.common.meter.log;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.util.ListUtils;
import redstone.multimeter.util.NbtUtils;

public class MeterLogs {
	
	private final List<EventLog>[] eventLogs;
	
	private long lastLoggedTick = -1;
	
	public MeterLogs() {
		@SuppressWarnings("unchecked")
		List<EventLog>[] lists = new List[EventType.ALL.length];
		
		for (int index = 0; index < lists.length; index++) {
			lists[index] = new ArrayList<>();
		}
		
		this.eventLogs = lists;
	}
	
	public void clear() {
		for (List<EventLog> logs : eventLogs) {
			logs.clear();
		}
		
		lastLoggedTick = -1;
	}
	
	public boolean isEmpty() {
		return lastLoggedTick < 0;
	}
	
	private List<EventLog> getLogs(EventType type) {
		return eventLogs[type.getIndex()];
	}
	
	public void add(EventLog log) {
		EventType type = log.getEvent().getType();
		List<EventLog> logs = getLogs(type);
		
		logs.add(log);
		
		if (log.getTick() > lastLoggedTick) {
			lastLoggedTick = log.getTick();
		}
	}
	
	public void clearOldLogs(long cutoff) {
		for (List<EventLog> logs : eventLogs) {
			while (!logs.isEmpty()) {
				EventLog log = logs.get(0);
				
				if (log.getTick() > cutoff) {
					break;
				}
				
				logs.remove(0);
			}
		}
	}
	
	public EventLog getLog(EventType type, int index) {
		if (index < 0) {
			return null;
		}
		
		List<EventLog> logs = getLogs(type);
		
		if (index >= logs.size()) {
			return null;
		}
		
		return logs.get(index);
	}
	
	public int getLastLogBefore(EventType type, long tick) {
		return getLastLogBefore(type, tick, 0);
	}
	
	public int getLastLogBefore(EventType type, long tick, int subick) {
		List<EventLog> logs = getLogs(type);
		
		if (logs.isEmpty() || !logs.get(0).isBefore(tick, subick)) {
			return -1;
		}
		if (tick > lastLoggedTick) {
			return logs.size() - 1;
		}
		
		int index = ListUtils.binarySearch(logs, event -> event.isBefore(tick, subick));
		EventLog log = logs.get(index);
		
		while (!log.isBefore(tick, subick)) {
			if (index == 0) {
				return -1;
			}
			
			log = logs.get(--index);
		}
		
		return index;
		
	}
	
	public EventLog getLastLogBefore(long tick) {
		return getLastLogBefore(tick, 0);
	}
	
	public EventLog getLastLogBefore(long tick, int subtick) {
		EventLog lastLog = null;
		
		for (EventType type : EventType.ALL) {
			int index = getLastLogBefore(type, tick, subtick);
			EventLog log = getLog(type, index);
			
			if (lastLog == null || (log != null && log.isAfter(lastLog))) {
				lastLog = log;
			}
		}
		
		return lastLog;
	}
	
	public EventLog getLogAt(long tick, int subtick) {
		EventLog log = getLastLogBefore(tick, subtick + 1);
		return log != null && log.isAt(tick, subtick) ? log : null;
	}
	
	public NBTTagCompound toNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (EventType type : EventType.ALL) {
			NBTTagList logs = toNbt(type);
			
			if (!logs.isEmpty()) {
				nbt.setTag(type.getName(), logs);
			}
		}
		
		return nbt;
	}
	
	private NBTTagList toNbt(EventType type) {
		NBTTagList list = new NBTTagList();
		
		for (EventLog log : getLogs(type)) {
			list.appendTag(log.toNbt());
		}
		
		return list;
	}
	
	public void updateFromNbt(NBTTagCompound nbt) {
		for (String key : nbt.getKeySet()) {
			EventType type = EventType.fromName(key);
			
			if (type != null) {
				updateFromNbt(type, nbt.getTagList(key, NbtUtils.TYPE_COMPOUND));
			}
		}
	}
	
	public void updateFromNbt(EventType type, NBTTagList logs) {
		for (int index = 0; index < logs.tagCount(); index++) {
			NBTTagCompound nbt = logs.getCompoundTagAt(index);
			EventLog log = EventLog.fromNbt(nbt);
			
			add(log);
		}
	}
}
