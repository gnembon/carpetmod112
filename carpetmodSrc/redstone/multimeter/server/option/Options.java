package redstone.multimeter.server.option;

import redstone.multimeter.common.meter.event.EventType;

public class Options {
	
	public Meter      meter       = new Meter();
	public MeterGroup meter_group = new MeterGroup();
	public EventTypes event_types = new EventTypes();
	
	public class Meter {
		
		public boolean allow_teleports = true;
		
	}
	
	public class MeterGroup {
		
		public int meter_limit   = -1;
		public int max_idle_time = 72000;
		
	}
	
	public class EventTypes {
		
		public String   allowed   = "all"; // "all", "blacklist", "whitelist"
		public String[] blacklist = { };
		public String[] whitelist = { };
		
	}
	
	protected transient int enabledEventTypes = ~0;
	
	public boolean hasEventType(EventType type) {
		return (enabledEventTypes & type.flag()) != 0;
	}
}
