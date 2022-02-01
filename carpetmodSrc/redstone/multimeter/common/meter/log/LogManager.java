package redstone.multimeter.common.meter.log;

import redstone.multimeter.common.meter.Meter;
import redstone.multimeter.common.meter.MeterGroup;

public abstract class LogManager {
	
	protected abstract MeterGroup getMeterGroup();
	
	protected abstract long getLastTick();
	
	public void clearLogs() {
		for (Meter meter : getMeterGroup().getMeters()) {
			meter.getLogs().clear();
		}
	}
}
