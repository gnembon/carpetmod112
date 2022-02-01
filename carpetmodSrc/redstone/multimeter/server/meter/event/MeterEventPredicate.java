package redstone.multimeter.server.meter.event;

import redstone.multimeter.common.meter.Meter;
import redstone.multimeter.common.meter.event.MeterEvent;
import redstone.multimeter.server.meter.ServerMeterGroup;

public interface MeterEventPredicate {
	
	public boolean test(ServerMeterGroup meterGroup, Meter meter, MeterEvent event);
	
}
