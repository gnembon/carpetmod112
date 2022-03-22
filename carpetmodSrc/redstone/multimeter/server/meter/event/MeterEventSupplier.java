package redstone.multimeter.server.meter.event;

import java.util.function.Supplier;

import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.common.meter.event.MeterEvent;

public class MeterEventSupplier {
	
	private final EventType type;
	private final Supplier<Integer> dataSupplier;
	
	private MeterEvent event;
	
	public MeterEventSupplier(EventType type, Supplier<Integer> dataSupplier) {
		this.type = type;
		this.dataSupplier = dataSupplier;
	}
	
	public EventType type() {
		return type;
	}
	
	public MeterEvent get() {
		if (event == null) {
			event = new MeterEvent(type, dataSupplier.get());
		}
		
		return event;
	}
}
