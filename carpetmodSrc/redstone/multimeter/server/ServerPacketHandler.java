package redstone.multimeter.server;

import java.util.Collection;

import carpet.CarpetSettings;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;

import redstone.multimeter.common.network.AbstractPacketHandler;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class ServerPacketHandler extends AbstractPacketHandler {
	
	private final MultimeterServer server;
	
	public ServerPacketHandler(MultimeterServer server) {
		this.server = server;
	}

	@Override
	protected Packet<?> toCustomPayload(String id, PacketBuffer buffer) {
		return new SPacketCustomPayload(id, buffer);
	}
	
	@Override
	public <P extends RSMMPacket> void send(P packet) {
		Packet<?> mcPacket = encode(packet);
		server.getPlayerManager().sendPacketToAllPlayers(mcPacket);
	}
	
	public <P extends RSMMPacket> void sendToPlayer(P packet, EntityPlayerMP player) {
		player.connection.sendPacket(encode(packet));
	}
	
	public <P extends RSMMPacket> void sendToPlayers(P packet, Collection<EntityPlayerMP> players) {
		Packet<?> mcPacket = encode(packet);
		
		for (EntityPlayerMP player : players) {
			player.connection.sendPacket(mcPacket);
		}
	}
	
	public <P extends RSMMPacket> void sendToSubscribers(P packet, ServerMeterGroup meterGroup) {
		sendToPlayers(packet, server.collectPlayers(meterGroup.getSubscribers()));
	}
	
	public void onPacketReceived(PacketBuffer buffer, EntityPlayerMP player) {
		try {
			RSMMPacket packet = decode(buffer);
			
			if (CarpetSettings.redstoneMultimeter || packet.force()) {
				packet.execute(server, player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
