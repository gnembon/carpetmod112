package carpet.carpetclient;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BiFunction;

import carpet.helpers.StackTraceDeobfuscator;

public class CarpetClientChunkLogger{
	
	static StackTraces stackTraces;
	static ChunkLoggerSerializer serializer;

	public static enum Event {
		MISSED_EVENT_ERROR,
		UNLOADING,
		LOADING,
		PLAYER_ENTERS,
		PLAYER_LEAVES,
		QUEUE_UNLOAD,
		CANCEL_UNLOAD,
		UNQUEUE_UNLOAD,
		GENERATING,
		POPULATING,
		GENERATING_STRUCTURES;
	}

	class ChunkLogEvent {
		int chunkX;
		int chunkZ;
		Event event;
		int stackTraceIndex;
		
		@Override
		public String toString() {
			return String.format("Chunk %d %d: %s, Trace:\n%s", chunkX, chunkZ, event.toString(),stackTraces.getString(stackTraceIndex));
		}
	}
	
	ArrayList<ChunkLogEvent> eventsThisGametick;
	HashMap<Long, ChunkLogEvent> lastEventForChunk;
	HashMap<Long, ChunkLogEvent> lastPlayerEventForChunk;
	
	public CarpetClientChunkLogger(){
		if(stackTraces == null) {
			stackTraces = new StackTraces();
			stackTraces.internString("");
		}
		if(serializer == null) {
			this.serializer = new ChunkLoggerSerializer();
		}
		String nullTrace = "";
		
		this.eventsThisGametick = new ArrayList();
		this.lastEventForChunk = new HashMap();
		this.lastPlayerEventForChunk = new HashMap();
	}
	
	public ArrayList<ChunkLogEvent> getInitialChunksForNewClient(){
		ArrayList<ChunkLogEvent> forNewClient = new ArrayList(this.lastEventForChunk.values());
		forNewClient.addAll(this.lastPlayerEventForChunk.values());
		return forNewClient;
	}
	
	public ArrayList<ChunkLogEvent> getEventsThisGametick() {
		return this.eventsThisGametick;
	}
	
	void log(int x, int z, Event event, int stackTrace) {
		ChunkLogEvent e = new ChunkLogEvent();
		e.chunkX = x;
		e.chunkZ = z;
		e.event = event;
		e.stackTraceIndex = stackTrace;
		this.eventsThisGametick.add(e);
		long l = ChunkPos.asLong(x, z);
		switch(event) {
		case MISSED_EVENT_ERROR:
			break;
		case PLAYER_LEAVES:
			this.lastPlayerEventForChunk.remove(l);
			break;
		case PLAYER_ENTERS:
			this.lastPlayerEventForChunk.put(l, e);
			break;
		case UNLOADING:
			this.lastEventForChunk.remove(l);
			break;
		default:
			this.lastEventForChunk.put(l, e);
		}
	}
	
	void logError(int x, int z, String customerror) {
		this.log(x, z, Event.MISSED_EVENT_ERROR, stackTraces.internString(customerror));
	}

	public void checkChunkStatus(World ww) {
		WorldServer w = (WorldServer) ww;
		ChunkProviderServer cp = (ChunkProviderServer)(w.getChunkProvider());
		PlayerChunkMap chunkmap = w.getPlayerChunkMap();
		Iterator<Chunk> i = chunkmap.getChunkIterator();
		
		while(i.hasNext()) {
			Chunk c = i.next();
			int x = c.x;
			int z = c.z;
			long l = ChunkPos.asLong(x, z);
			ChunkLogEvent currentEntry = this.lastEventForChunk.get(l);
			ChunkLogEvent currentPlayerEntry = this.lastPlayerEventForChunk.get(l);
			
			boolean contains = chunkmap.contains(x, z);
			if((currentPlayerEntry == null) && contains){
				logError(x,z,"PLAYER_ENTER");
			}
			else if((currentPlayerEntry != null) && !contains) {
				logError(x,z,"PLAYER_LEAVE");
			}
			if(currentEntry == null) {
				 logError(x,z,"LOADING");
			}
			else {
				if(cp.isChunkUnloadScheduled(x, z)) {
					if(c.unloadQueued) {
						if(currentEntry.event != Event.QUEUE_UNLOAD) {
							logError(x,z,"QUEUE_UNLOAD");
						}
					}
					else {
						if(currentEntry.event != Event.CANCEL_UNLOAD) {
							logError(x,z,"CANCEL_UNLOAD");
						}
					}
				}
				else {
					if(currentEntry.event == Event.QUEUE_UNLOAD ||
					   currentEntry.event == Event.CANCEL_UNLOAD) {
						logError(x,z,"UNQUEUE_UNLOAD");
					}
				}
			}
			for(ChunkLogEvent event:this.lastEventForChunk.values()) {
				if(!cp.chunkExists(event.chunkX, event.chunkZ)){
					logError(x,z,"UNLOAD");
				}
			}
		}
	}
	
	public void startTick() {
		this.eventsThisGametick.clear();
		stackTraces.startTick();
	}
	
	public static void sendAll() {
		serializer.sendUpdates();
	}

	public static void log(World w, int x, int z, Event e) {
		int stacktraceid = 0;
		if(serializer.hasStackTraceListeners()) {
			stacktraceid = stackTraces.internStackTrace();
		}
		w.chunklogger.log(x, z, e, stacktraceid);
	}

	public class StackTraces {
		// TODO: make version configurable 
		StackTraceDeobfuscator deobf = StackTraceDeobfuscator.create().withMinecraftVersion("1.12").withSnapshotMcpNames("20180713-1.12");		
		HashMap<String,Integer> stackTraceToIndex = new HashMap();
		ArrayList<String> allTracesDeobfuscated = new ArrayList();
		ArrayList<String> newTracesDeobfuscated = new ArrayList();

		public void startTick() {
			this.newTracesDeobfuscated.clear();
		}

		public String getString(int i) {
			return this.allTracesDeobfuscated.get(i);
		}
		
		private ArrayList<String> getInitialStackTracesForNewClient(){
			return this.allTracesDeobfuscated;
		}
		
		private ArrayList<String> getNewStackTraces() {
			return this.newTracesDeobfuscated;
		}
		
		private int getStackTracesCount() {
			return this.allTracesDeobfuscated.size();
		}
		
		private int internString(String obfuscated, String deobfuscated) {
			Integer i = stackTraceToIndex.get(obfuscated);
			if(i == null) {
				i = this.allTracesDeobfuscated.size();
				this.allTracesDeobfuscated.add(deobfuscated);
				this.newTracesDeobfuscated.add(deobfuscated);
				stackTraceToIndex.put(obfuscated, i);
			}
			return i;
		}
		
		private int internString(String s) {
			return this.internString(s,s);
		}
		
		private int internStackTrace() {
			StackTraceElement[] trace = new Throwable().getStackTrace();
			String obfuscated = asString(trace,false);
			Integer i = this.stackTraceToIndex.get(obfuscated);
			if(i != null) {
				return i;
			}
			else {
				String deobfuscated = asString(trace, true);
				return this.internString(obfuscated, deobfuscated);
			}
		}
		
		private String asString(StackTraceElement[] trace, boolean deobfuscated) {
			if(deobfuscated) {
				trace =  deobf.withStackTrace(trace).deobfuscate();
			}
			String stacktrace = new String();
			for(StackTraceElement e:trace) {
				if("CarpetClientChunkLogger.java".equals(e.getFileName())) {
					continue;
				}
				if(!stacktrace.isEmpty()) {
					stacktrace += System.lineSeparator();
				}
				stacktrace += e.toString();
			}
			return stacktrace;
		}
	}
	
	class ChunkLoggerSerializer {
		
		public static final int PACKET_EVENTS = 0;
		public static final int PACKET_STACKTRACE = 1;

		private HashSet<EntityPlayerMP> playersLoggingChunks = new HashSet();
		private HashSet<EntityPlayerMP> playersGettingStackTraces = new HashSet();
		
		private final BiFunction<World,Integer,NBTTagCompound> initalChunksSerializer = (World w, Integer dimension) -> {
			return serializeEvents(w.chunklogger.getInitialChunksForNewClient(), dimension);
		};
		private final BiFunction<World,Integer,NBTTagCompound> updateChunksSerializer = (World w, Integer dimension) -> {
			return serializeEvents(w.chunklogger.getEventsThisGametick(), dimension);
		};
		
		public void registerPlayer(EntityPlayerMP sender, PacketBuffer data) {
			boolean addPlayer = data.readBoolean();
			boolean getStackTraces = data.readBoolean();
			if (addPlayer) {
				if(getStackTraces) {
					playersGettingStackTraces.add(sender);
					this.sendInitalStackTraces(sender);
				}
				playersLoggingChunks.add(sender);
				this.sendInitalChunks(sender);
			} else {
				this.unregisterPlayer(sender);
			}
		}
		
		public void unregisterPlayer(EntityPlayerMP player) {
			playersLoggingChunks.remove(player);
			playersGettingStackTraces.remove(player);
		}
		
		private void sendInitalChunks(EntityPlayerMP sender) {
			NBTTagCompound data = serializeAllDimensions(sender.getServer(), initalChunksSerializer);
			CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_EVENTS, data);
		}
		
		private void sendInitalStackTraces(EntityPlayerMP sender) {
			NBTTagCompound stackData = serializeStackTraces(stackTraces.getInitialStackTracesForNewClient(),0);
			if(stackData != null) {
				CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_STACKTRACE, stackData);
			}
		}
		
		private boolean hasStackTraceListeners() {
			return !this.playersGettingStackTraces.isEmpty();
		}
		
		private void sendUpdates() {
			if(this.playersLoggingChunks.isEmpty()) {
				return;
			}
			MinecraftServer server = this.playersLoggingChunks.iterator().next().server;
			NBTTagCompound chunkData = serializeAllDimensions(server, updateChunksSerializer);
			if(chunkData != null) {
				for(EntityPlayerMP player: this.playersLoggingChunks) {
					CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_EVENTS, chunkData);
				}
			}
			
			if(this.playersGettingStackTraces.isEmpty()) {
				return;
			}
			ArrayList<String> traces = stackTraces.getNewStackTraces();
			int tracesStartId = stackTraces.getStackTracesCount() - traces.size();
			NBTTagCompound stackData = serializeStackTraces(traces, tracesStartId);
			if(stackData != null) {
				for(EntityPlayerMP player: this.playersGettingStackTraces) {
					CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_STACKTRACE, stackData);
				}
			}
		}
		
		private NBTTagCompound serializeAllDimensions(MinecraftServer server, BiFunction<World,Integer,NBTTagCompound> f) {
			NBTTagList list = new NBTTagList();
			int i = 0;
			for(World w : server.worlds) {
				NBTTagCompound c = f.apply(w,i);
				if(c != null) {
					list.appendTag(c);
				}
				++i;
			}
			if(list.isEmpty()) {
				return null;
			}
			NBTTagCompound data = new NBTTagCompound();
			int time = server.getTickCounter();
			data.setTag("dimensionData", list);
			data.setInteger("time", time);
			return data;
		}
		
		private NBTTagCompound serializeEvents(ArrayList<ChunkLogEvent> events, int dimension) {
			if(events.isEmpty()) {
				return null;
			}
			NBTTagCompound chunkData = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (ChunkLogEvent event : events) {
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("chunkX", event.chunkX);
				data.setInteger("chunkZ", event.chunkZ);
				data.setInteger("status", event.event.ordinal());
				data.setInteger("stackTraceIndex", event.stackTraceIndex);
				list.appendTag(data);
			}
			chunkData.setTag("chunkData", list);
			chunkData.setInteger("dimension", dimension);
			chunkData.setTag("data", chunkData);
			return chunkData;
		}

		private NBTTagCompound serializeStackTraces(ArrayList<String> traces, int startId) {
			if(traces.isEmpty()) {
				return null;
			}
			NBTTagList list = new NBTTagList();
			int i = 0;
			for(String s: traces) {
				NBTTagCompound stackTrace = new NBTTagCompound();
				stackTrace.setInteger("id", startId+i);
				stackTrace.setString("stack", s);
				list.appendTag(stackTrace);
				++i;
			}
			NBTTagCompound stackList = new NBTTagCompound();
			stackList.setTag("stackList", list);
			return stackList;
		}
	}
}
