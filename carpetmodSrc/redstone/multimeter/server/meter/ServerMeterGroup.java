package redstone.multimeter.server.meter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.Meter;
import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.meter.MeterProperties.MutableMeterProperties;
import redstone.multimeter.common.meter.event.MeterEvent;
import redstone.multimeter.common.network.packets.MeterUpdatesPacket;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.meter.event.MeterEventPredicate;
import redstone.multimeter.server.meter.event.MeterEventSupplier;
import redstone.multimeter.server.meter.log.ServerLogManager;

public class ServerMeterGroup extends MeterGroup {
	
	private final Multimeter multimeter;
	private final ServerLogManager logManager;
	
	private final UUID owner;
	private final Set<UUID> members;
	private final Set<UUID> subscribers;
	
	private final List<Long> removedMeters;
	private final Map<Long, MeterProperties> meterUpdates;
	
	private boolean isPrivate;
	private boolean idle;
	private long idleTime;
	
	public ServerMeterGroup(Multimeter multimeter, String name, EntityPlayerMP owner) {
		super(name);
		
		this.multimeter = multimeter;
		this.logManager = new ServerLogManager(this);
		
		this.owner = owner.getUniqueID();
		this.members = new HashSet<>();
		this.subscribers = new HashSet<>();
		
		this.removedMeters = new ArrayList<>();
		this.meterUpdates = new LinkedHashMap<>();
		
		this.isPrivate = false;
		this.idle = true;
		this.idleTime = 0L;
	}
	
	@Override
	public void clear() {
		super.clear();
		
		removedMeters.clear();
		meterUpdates.clear();
	}
	
	@Override
	protected void moveMeter(Meter meter, DimPos newPos) {
		if (hasMeterAt(newPos)) {
			return;
		}
		
		World world = multimeter.getMultimeterServer().getWorldOf(newPos);
		
		if (world == null) {
			return;
		}
		
		super.moveMeter(meter, newPos);
	}
	
	@Override
	protected void meterAdded(Meter meter) {
		meterUpdates.putIfAbsent(meter.getId(), meter.getProperties());
	}
	
	@Override
	protected void meterRemoved(Meter meter) {
		removedMeters.add(meter.getId());
		meterUpdates.remove(meter.getId());
	}
	
	@Override
	protected void meterUpdated(Meter meter) {
		meterUpdates.putIfAbsent(meter.getId(), meter.getProperties());
	}
	
	@Override
	public ServerLogManager getLogManager() {
		return logManager;
	}
	
	public Multimeter getMultimeter() {
		return multimeter;
	}
	
	public boolean addMeter(MutableMeterProperties properties) {
		return addMeter(new Meter(properties));
	}
	
	public boolean removeMeter(long id) {
		return hasMeter(id) && removeMeter(getMeter(id));
	}
	
	public boolean updateMeter(long id, MeterProperties newProperties) {
		return hasMeter(id) && updateMeter(getMeter(id), newProperties);
	}
	
	public void tryMoveMeter(DimPos pos, EnumFacing dir) {
		if (!hasMeterAt(pos)) {
			return;
		}
		
		Meter meter = getMeterAt(pos);
		
		if (!meter.isMovable()) {
			return;
		}
		
		moveMeter(meter, pos.offset(dir));
	}
	
	public boolean isPastMeterLimit() {
		int limit = multimeter.options.meter_group.meter_limit;
		return limit >= 0 && getMeters().size() >= limit;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public boolean isOwnedBy(EntityPlayerMP player) {
		return isOwnedBy(player.getUniqueID());
	}
	
	public boolean isOwnedBy(UUID playerUUID) {
		return owner.equals(playerUUID);
	}
	
	public boolean hasMembers() {
		return !members.isEmpty();
	}
	
	public Collection<UUID> getMembers() {
		return Collections.unmodifiableCollection(members);
	}
	
	public boolean hasMember(EntityPlayerMP player) {
		return hasMember(player.getUniqueID());
	}
	
	public boolean hasMember(UUID playerUUID) {
		return members.contains(playerUUID);
	}
	
	public void addMember(UUID playerUUID) {
		members.add(playerUUID);
	}
	
	public void removeMember(UUID playerUUID) {
		members.remove(playerUUID);
	}
	
	public void clearMembers() {
		members.clear();
	}
	
	public boolean hasSubscribers() {
		return !subscribers.isEmpty();
	}
	
	public Collection<UUID> getSubscribers() {
		return Collections.unmodifiableCollection(subscribers);
	}
	
	public boolean hasSubscriber(EntityPlayerMP player) {
		return hasSubscriber(player.getUniqueID());
	}
	
	public boolean hasSubscriber(UUID playerUUID) {
		return subscribers.contains(playerUUID);
	}
	
	public void addSubscriber(UUID playerUUID) {
		subscribers.add(playerUUID);
	}
	
	public void removeSubscriber(UUID playerUUID) {
		subscribers.remove(playerUUID);
	}
	
	public boolean isPrivate() {
		return isPrivate;
	}
	
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
		
		if (isPrivate) {
			for (UUID playerUUID : subscribers) {
				if (playerUUID != owner) {
					addMember(playerUUID);
				}
			}
		}
	}
	
	public boolean isIdle() {
		return idle;
	}
	
	public long getIdleTime() {
		return idleTime;
	}
	
	public boolean updateIdleState() {
		boolean wasIdle = idle;
		idle = !hasSubscribers();
		
		if (wasIdle && !idle) {
			idleTime = 0L;
		}
		
		return wasIdle != idle;
	}
	
	public boolean isPastIdleTimeLimit() {
		return idle && multimeter.options.meter_group.max_idle_time >= 0 && idleTime > multimeter.options.meter_group.max_idle_time;
	}
	
	public void tick() {
		if (idle) {
			idleTime++;
		}
		
		logManager.tick();
	}
	
	public void flushUpdates() {
		if (removedMeters.isEmpty() && meterUpdates.isEmpty()) {
			return;
		}
		
		MeterUpdatesPacket packet = new MeterUpdatesPacket(removedMeters, meterUpdates);
		multimeter.getMultimeterServer().getPacketHandler().sendToSubscribers(packet, this);
		
		removedMeters.clear();
		meterUpdates.clear();
	}
	
	public void tryLogEvent(DimPos pos, MeterEventPredicate predicate, MeterEventSupplier supplier) {
		if (hasMeterAt(pos)) {
			Meter meter = getMeterAt(pos);
			
			if (meter.isMetering(supplier.type())) {
				MeterEvent event = supplier.get();
				
				if (predicate.test(this, meter, event)) {
					logManager.logEvent(meter, supplier.get());
				}
			}
		}
	}
}
