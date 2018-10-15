package carpet.carpetclient;
/*
 *  Authors: Xcom and 0x53ee71ebe11e 
 *  
 *  Backend for the the carpetclient chunk debugging tool by
 *  Earthcomputer, Xcom and 0x53ee71ebe11e
 *  
 */

import carpet.CarpetSettings;
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
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import carpet.helpers.StackTraceDeobfuscator;

public class CarpetClientChunkLogger {

    public static CarpetClientChunkLogger logger = new CarpetClientChunkLogger();
    public boolean enabled = true;
    
    StackTraces stackTraces;
    private ChunkLoggerSerializer clients;
    private ArrayList<ChunkLog> eventsThisGametick;

    public static enum Event {
        NONE,
        UNLOADING,
        LOADING,
        PLAYER_ENTERS,
        PLAYER_LEAVES,
        QUEUE_UNLOAD,
        CANCEL_UNLOAD,
        GENERATING,
        POPULATING,
        GENERATING_STRUCTURES;
    }

    private static class ChunkLog {
    	final int chunkX;
        final int chunkZ;
        final int chunkDimension;
        final Event event;
        final int stackTraceIndex;

        ChunkLog(int x, int z, int d, Event e, int trace) {
        	this.chunkX = x;
        	this.chunkZ = z;
        	this.chunkDimension = d;
            this.event = e;
            this.stackTraceIndex = trace;
        }
    }

    public CarpetClientChunkLogger() {
        if (stackTraces == null) {
            stackTraces = new StackTraces();
        }
        if (clients == null) {
            this.clients = new ChunkLoggerSerializer();
        }
        String nullTrace = "";

        this.eventsThisGametick = new ArrayList();
    }
    
    /*
     * main logging function
     * logs a change in a chunk including a stacktrace if required by the client 
     */
    public void log(World w, int x, int z, Event e) {
        int stacktraceid = 0;
        if (clients.hasStackTraceListeners()) {
            stacktraceid = stackTraces.internStackTrace();
        }
        log(x, z, getWorldIndex(w), e, stacktraceid);
    }

    /*
     * called at the end of a gametick to send all events to the registered clients 
     */
    public void sendAll() {
        clients.sendUpdates();
        this.eventsThisGametick.clear();
    }

    /*
     * removes all players and disables the logging
     */
    public void disable() {
        enabled = false;
        clients.kickAllPlayers();
        this.eventsThisGametick.clear();
    }
    

    /*
     * called when registering a new player
     */
    public void registerPlayer(EntityPlayerMP sender, PacketBuffer data) {
    	clients.registerPlayer(sender, data);
    }
    
    /*
     * unregisters a single player
     */
    public void unregisterPlayer(EntityPlayerMP player) {
    	clients.unregisterPlayer(player);
    }

    private ArrayList<ChunkLog> getInitialChunksForNewClient(MinecraftServer server) {
        ArrayList<ChunkLog> forNewClient = new ArrayList();
        int dimension = -1;
        for (World w : server.worlds) {
            ChunkProviderServer provider = (ChunkProviderServer) (w.getChunkProvider());
            dimension++;
            for (Chunk c : provider.getLoadedChunks()) {
                forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.LOADING, 0));
                if (provider.isChunkUnloadScheduled(c.x, c.z)) {
                    forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.QUEUE_UNLOAD, 0));
                    if (!c.unloadQueued) {
                        forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.CANCEL_UNLOAD, 0));
                    }
                }
            }
            PlayerChunkMap chunkmap = ((WorldServer) w).getPlayerChunkMap();
            Iterator<ChunkPos> i = chunkmap.carpetGetAllChunkCoordinates();
            while (i.hasNext()) {
                ChunkPos pos = i.next();
                forNewClient.add(new ChunkLog(pos.x, pos.z, dimension, Event.PLAYER_ENTERS, 0));
            }
        }
        return forNewClient;
    }

    private ArrayList<ChunkLog> getEventsThisGametick() {
        return this.eventsThisGametick;
    }

    private void log(int x, int z, int d, Event event, int stackTrace) {
        this.eventsThisGametick.add(new ChunkLog(x, z, d, event, stackTrace));
    }

    private static int getWorldIndex(World w) {
        int i = 0;
        for (World o : w.getMinecraftServer().worlds) {
            if (o == w) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private class StackTraces {
        StackTraceDeobfuscator deobf = StackTraceDeobfuscator.create()
                .withMinecraftVersion(CarpetSettings.minecraftVersion)
                .withSnapshotMcpNames(CarpetSettings.mcpMappings);
        HashMap<String, Integer> stackTraceToIndex = new HashMap();
        ArrayList<String> allTracesDeobfuscated = new ArrayList();

        private StackTraces() {
            clear();
        }

        private void clear() {
            stackTraceToIndex.clear();
            allTracesDeobfuscated.clear();
            this.internString("");
        }

        private String getString(int i) {
        	if((i<0) || (i>=this.allTracesDeobfuscated.size())) {
        		return null;
        	}
        	else {
        		return this.allTracesDeobfuscated.get(i);
        	}
        }

        private ArrayList<String> getInitialStackTracesForNewClient() {
            return this.allTracesDeobfuscated;
        }

        private int getStackTracesCount() {
            return this.allTracesDeobfuscated.size();
        }

        private int internString(String obfuscated, String deobfuscated) {
            Integer i = stackTraceToIndex.get(obfuscated);
            if (i == null) {
                i = this.allTracesDeobfuscated.size();
                this.allTracesDeobfuscated.add(deobfuscated);
                stackTraceToIndex.put(obfuscated, i);
            }
            return i;
        }

        private int internString(String s) {
            return this.internString(s, s);
        }

        private int internStackTrace() {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            String obfuscated = asString(trace, false);
            Integer i = this.stackTraceToIndex.get(obfuscated);
            if (i != null) {
                return i;
            } else {
                String deobfuscated = asString(trace, true);
                return this.internString(obfuscated, deobfuscated);
            }
        }

        private String asString(StackTraceElement[] trace, boolean deobfuscated) {
            if (deobfuscated) {
                trace = deobf.withStackTrace(trace).deobfuscate();
            }
            String stacktrace = new String();
            for (StackTraceElement e : trace) {
                if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                    continue;
                }
                if (!stacktrace.isEmpty()) {
                    stacktrace += "\n";
                }
                stacktrace += e.toString();
            }
            return stacktrace;
        }
    }

    private class ChunkLoggerSerializer {

        private static final int PACKET_EVENTS = 0;
        private static final int PACKET_STACKTRACE = 1;
        private static final int PACKET_ACCESS_DENIED = 2;
 
        private static final int STACKTRACES_BATCH_SIZE = 10;
        private static final int LOGS_BATCH_SIZE = 1000;

        private HashSet<EntityPlayerMP> playersLoggingChunks = new HashSet();
        private HashMap<EntityPlayerMP, HashSet<Integer>> sentTracesForPlayer = new HashMap();

        public void registerPlayer(EntityPlayerMP sender, PacketBuffer data) {
            if (!CarpetSettings.chunkDebugTool) {
                CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_ACCESS_DENIED, new NBTTagCompound());
                return;
            }
            boolean addPlayer = data.readBoolean();
            boolean getStackTraces = data.readBoolean();
            if (addPlayer) {
                enabled = true;
                if(getStackTraces) {
                	this.sentTracesForPlayer.put(sender, new HashSet<Integer>());
                }
                playersLoggingChunks.add(sender);
                this.sendInitalChunks(sender);
            } else {
                this.unregisterPlayer(sender);
            }
        }

        public void unregisterPlayer(EntityPlayerMP player) {
            playersLoggingChunks.remove(player);
            sentTracesForPlayer.remove(player);
            if (!hasStackTraceListeners()) {
                stackTraces.clear();
            }
            if (playersLoggingChunks.isEmpty()) {
                enabled = false;
            }
        }
        
        public boolean hasListeners() {
            return !this.playersLoggingChunks.isEmpty();
        }

        private boolean hasStackTraceListeners() {
            return !this.sentTracesForPlayer.isEmpty();
        }
        
        private void sendInitalChunks(EntityPlayerMP sender) {
            MinecraftServer server = sender.getServer();
            ArrayList<ChunkLog> logs = getInitialChunksForNewClient(server);
            sendMissingStackTracesForPlayer(sender,logs);
            for(int i = 0; i < logs.size(); i += LOGS_BATCH_SIZE) {
            	boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
            	List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
            	NBTTagCompound chunkData = serializeEvents(batch, -server.getTickCounter() -1, i, complete);
            	if(chunkData != null) {
            		CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_EVENTS, chunkData);
            	}
            }
        }

        private void sendUpdates() {
            if (this.playersLoggingChunks.isEmpty()) {
                return;
            }
            
            ArrayList<ChunkLog> logs = getEventsThisGametick();
            MinecraftServer server = this.playersLoggingChunks.iterator().next().server;
            
            for (EntityPlayerMP client : this.sentTracesForPlayer.keySet()) {
            	this.sendMissingStackTracesForPlayer(client, logs);
            }
            for(int i = 0; i < logs.size() ; i += LOGS_BATCH_SIZE) {
            	boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
            	List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
            	NBTTagCompound chunkData = serializeEvents(batch, server.getTickCounter(), i, complete);
		        if (chunkData != null) {
		            for (EntityPlayerMP player : this.playersLoggingChunks) {
		            	CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_EVENTS, chunkData);
		            }
		        }
            }
        }
        
        private void sendMissingStackTracesForPlayer(EntityPlayerMP player, ArrayList<ChunkLog> events) {
        	HashSet<Integer> missingTraces = new HashSet<Integer>();
        	HashSet<Integer> sentTraces = this.sentTracesForPlayer.getOrDefault(player, null);
        	
        	if(sentTraces == null) {
        		return;
        	}
        	for(ChunkLog log : events) {
        		int id = log.stackTraceIndex;
        		if(!sentTraces.contains(id)) {
        			sentTraces.add(id);
        			missingTraces.add(id);
        			System.err.println(String.format("Sending trace %d to player %s",id , player.getName()));
        		}
        	}
        	ArrayList missingList = new ArrayList(missingTraces);
        	for(int i = 0; i < missingList.size(); i += STACKTRACES_BATCH_SIZE) {
        		List<Integer> part = missingList.subList(i, Integer.min(i + STACKTRACES_BATCH_SIZE, missingList.size()));
        		NBTTagCompound stackData = serializeStackTraces(part);
        		if(stackData != null) {
        			CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_STACKTRACE, stackData);
        		}
        	}
        }

        private NBTTagCompound serializeEvents(List<ChunkLog> events, int gametick, int dataOffset, boolean complete) {
            if (events.isEmpty()) {
                return null;
            }
            NBTTagCompound chunkData = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (ChunkLog log : events) {
                NBTTagCompound data = new NBTTagCompound();
                data.setInteger("x", log.chunkX);
                data.setInteger("z", log.chunkZ);
                data.setInteger("d", log.chunkDimension);
                data.setInteger("event", log.event.ordinal());
                data.setInteger("trace", log.stackTraceIndex);
                list.appendTag(data);
            }
            chunkData.setTag("data", list);
            chunkData.setInteger("offset", dataOffset);
            chunkData.setInteger("time", gametick);
            chunkData.setBoolean("complete", complete);
            return chunkData;
        }

        private NBTTagCompound serializeStackTraces(List<Integer> ids) {
            if (ids.isEmpty()) {
                return null;
            }
            NBTTagList list = new NBTTagList();
            for (Integer id : ids) {
            	String s = stackTraces.getString(id);
                NBTTagCompound stackTrace = new NBTTagCompound();
                stackTrace.setInteger("id", id);
                stackTrace.setString("stack", s);
                list.appendTag(stackTrace);
            }
            NBTTagCompound stackList = new NBTTagCompound();
            stackList.setTag("stackList", list);
            return stackList;
        }

        private void kickAllPlayers() {
            for (EntityPlayerMP player : playersLoggingChunks) {
                CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_ACCESS_DENIED, new NBTTagCompound());
            }
            this.playersLoggingChunks.clear();
            this.sentTracesForPlayer.clear();
        }
    }
}
