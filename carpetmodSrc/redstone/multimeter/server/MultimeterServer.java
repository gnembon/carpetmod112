package redstone.multimeter.server;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import carpet.CarpetSettings;
import carpet.helpers.TickSpeed;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.TickPhase;
import redstone.multimeter.common.TickPhaseTree;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.common.network.packets.HandshakePacket;
import redstone.multimeter.common.network.packets.ServerTickPacket;
import redstone.multimeter.common.network.packets.TickPhaseTreePacket;
import redstone.multimeter.server.meter.ServerMeterGroup;
import redstone.multimeter.util.DimensionUtils;

public class MultimeterServer {
	
	private final MinecraftServer server;
	private final ServerPacketHandler packetHandler;
	private final Multimeter multimeter;
	private final Map<UUID, String> connectedPlayers;
	private final Map<UUID, String> playerNameCache;
	private final TickPhaseTree tickPhaseTree;
	
	private TickPhase tickPhase;
	/** true if the OverWorld already ticked time */
	private boolean tickedTime;
	
	public MultimeterServer(MinecraftServer server) {
		this.server = server;
		this.packetHandler = new ServerPacketHandler(this);
		this.multimeter = new Multimeter(this);
		this.connectedPlayers = new HashMap<>();
		this.playerNameCache = new HashMap<>();
		this.tickPhaseTree = new TickPhaseTree();
		
		this.tickPhase = TickPhase.UNKNOWN;
		this.tickedTime = false;
	}
	
	public MinecraftServer getMinecraftServer() {
		return server;
	}
	
	public ServerPacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	public Multimeter getMultimeter() {
		return multimeter;
	}
	
	public TickPhaseTree getTickPhaseTree() {
		return tickPhaseTree;
	}
	
	public boolean isDedicated() {
		return server.isDedicatedServer();
	}
	
	public File getConfigFolder() {
		return new File(server.getDataDirectory(), RedstoneMultimeter.CONFIG_PATH);
	}
	
	public TickPhase getTickPhase() {
		return tickPhase;
	}
	
	public void startTickTask(boolean updateTree, TickTask task, String... args) {
		tickPhase = tickPhase.startTask(task);
		if (updateTree) {
			tickPhaseTree.startTask(task, args);
		}
	}
	
	public void endTickTask(boolean updateTree) {
		tickPhase = tickPhase.endTask();
		if (updateTree) {
			tickPhaseTree.endTask();
		}
	}
	
	public void swapTickTask(boolean updateTree, TickTask task, String... args) {
		tickPhase = tickPhase.swapTask(task);
		if (updateTree) {
			tickPhaseTree.swapTask(task, args);
		}
	}
	
	public void onOverworldTickTime() {
		tickedTime = true;
	}
	
	public long getCurrentTick() {
		long tick = server.getWorld(DimensionType.OVERWORLD.getId()).getTotalWorldTime();
		
		if (!tickedTime) {
			tick++;
		}
		
		return tick;
	}
	
	public boolean isPaused() {
		return server.isPaused() || !TickSpeed.process_entities;
	}
	
	public void tickStart() {
		boolean paused = isPaused();
		
		if (!paused) {
			tickedTime = false;
			
			if (server.getTickCounter() % 72000 == 0) {
				cleanPlayerNameCache();
			}
			if (shouldBuildTickPhaseTree()) {
				tickPhaseTree.start();
			}
		}
		
		tickPhase = TickPhase.UNKNOWN;
		multimeter.tickStart(paused);
	}
	
	private void cleanPlayerNameCache() {
		playerNameCache.keySet().removeIf(playerUUID -> {
			for (ServerMeterGroup meterGroup : multimeter.getMeterGroups()) {
				if (meterGroup.hasMember(playerUUID)) {
					return false;
				}
			}
			
			return true;
		});
	}
	
	private boolean shouldBuildTickPhaseTree() {
		return CarpetSettings.redstoneMultimeter && !tickPhaseTree.isComplete() && !tickPhaseTree.isBuilding();
	}
	
	public void tickEnd() {
		boolean paused = isPaused();
		
		if (!paused) {
			ServerTickPacket packet = new ServerTickPacket(getCurrentTick());
			
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				if (multimeter.hasSubscription(player)) {
					packetHandler.sendToPlayer(packet, player);
				}
			}
		}
		if (tickPhaseTree.isBuilding()) {
			tickPhaseTree.end();
		}
		
		tickPhase = TickPhase.UNKNOWN;
		multimeter.tickEnd(paused);
	}
	
	public void onPlayerJoin(EntityPlayerMP player) {
		multimeter.onPlayerJoin(player);
		playerNameCache.remove(player.getUniqueID());
	}
	
	public void onPlayerLeave(EntityPlayerMP player) {
		multimeter.onPlayerLeave(player);
		connectedPlayers.remove(player.getUniqueID());
		playerNameCache.put(player.getUniqueID(), player.getName());
	}
	
	public void onHandshake(EntityPlayerMP player, String modVersion) {
		if (connectedPlayers.put(player.getUniqueID(), modVersion) == null) {
			HandshakePacket packet = new HandshakePacket();
			packetHandler.sendToPlayer(packet, player);
			
			refreshTickPhaseTree(player);
		}
	}
	
	public void refreshTickPhaseTree(EntityPlayerMP player) {
		if (tickPhaseTree.isComplete()) {
			TickPhaseTreePacket packet = new TickPhaseTreePacket(tickPhaseTree.toNbt());
			packetHandler.sendToPlayer(packet, player);
		}
	}
	
	public WorldServer getWorld(ResourceLocation dimensionId) {
		DimensionType type = DimensionUtils.getType(dimensionId);
		return server.getWorld(type.getId());
	}
	
	public WorldServer getWorldOf(DimPos pos) {
		return getWorld(pos.getDimensionId());
	}
	
	public IBlockState getBlockState(DimPos pos) {
		World world = getWorldOf(pos);
		
		if (world != null) {
			return world.getBlockState(pos.getBlockPos());
		}
		
		return null;
	}
	
	public PlayerList getPlayerManager() {
		return server.getPlayerList();
	}
	
	public EntityPlayerMP getPlayer(UUID playerUUID) {
		return server.getPlayerList().getPlayerByUUID(playerUUID);
	}
	
	public String getPlayerName(UUID playerUUID) {
		EntityPlayerMP player = getPlayer(playerUUID);
		return player == null ? playerNameCache.get(playerUUID) : player.getName();
	}
	
	public EntityPlayerMP getPlayer(String playerName) {
		return server.getPlayerList().getPlayerByUsername(playerName);
	}
	
	public boolean isMultimeterClient(UUID playerUUID) {
		return connectedPlayers.containsKey(playerUUID);
	}
	
	public boolean isMultimeterClient(EntityPlayerMP player) {
		return connectedPlayers.containsKey(player.getUniqueID());
	}
	
	public Collection<EntityPlayerMP> collectPlayers(Collection<UUID> playerUUIDs) {
		Set<EntityPlayerMP> players = new LinkedHashSet<>();
		
		for (UUID playerUUID : playerUUIDs) {
			EntityPlayerMP player = getPlayer(playerUUID);
			
			if (player != null) {
				players.add(player);
			}
		}
		
		return players;
	}
	
	public void sendMessage(EntityPlayerMP player, ITextComponent message, boolean actionBar) {
		player.sendStatusMessage(message, actionBar);
	}
}
