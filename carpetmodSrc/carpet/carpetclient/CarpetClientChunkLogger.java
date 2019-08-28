package carpet.carpetclient;
/*
 *  Authors: Xcom and 0x53ee71ebe11e
 *
 *  Backend for the the carpetclient chunk debugging tool by
 *  Earthcomputer, Xcom and 0x53ee71ebe11e
 *
 */

import carpet.CarpetSettings;
import carpet.helpers.StackTraceDeobfuscator;
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

import java.util.*;

public class CarpetClientChunkLogger {
    public static CarpetClientChunkLogger logger = new CarpetClientChunkLogger();

    public boolean enabled = true;
    StackTraces stackTraces;
    private ChunkLoggerSerializer clients;
    private ArrayList<ChunkLog> eventsThisGametick;
    public static String reason = null;
    public static String oldReason = null;

    private final int MAX_STACKTRACE_SIZE = 60;

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
        final int reasonID;

        ChunkLog(int x, int z, int d, Event e, int trace, int reason) {
            this.chunkX = x;
            this.chunkZ = z;
            this.chunkDimension = d;
            this.event = e;
            this.stackTraceIndex = trace;
            this.reasonID = reason;
        }
    }

    public static void resetToOldReason() {
        reason = oldReason;
    }

    public static void setReason(String r) {
        oldReason = reason;
        reason = r;
    }

    public static void resetReason() {
        reason = null;
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
        int stacktraceid = stackTraces.internStackTrace();
        int reasonId = stackTraces.internReason();
        log(x, z, getWorldIndex(w), e, stacktraceid, reasonId);
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
                forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.LOADING, 0, 0));
                if (provider.isChunkUnloadScheduled(c.x, c.z)) {
                    forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.QUEUE_UNLOAD, 0, 0));
                    if (!c.unloadQueued) {
                        forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.CANCEL_UNLOAD, 0, 0));
                    }
                }
            }
            PlayerChunkMap chunkmap = ((WorldServer) w).getPlayerChunkMap();
            Iterator<ChunkPos> i = chunkmap.carpetGetAllChunkCoordinates();
            while (i.hasNext()) {
                ChunkPos pos = i.next();
                forNewClient.add(new ChunkLog(pos.x, pos.z, dimension, Event.PLAYER_ENTERS, 0, 0));
            }
        }
        return forNewClient;
    }

    private ArrayList<ChunkLog> getEventsThisGametick() {
        return this.eventsThisGametick;
    }

    private void log(int x, int z, int d, Event event, int stackTrace, int reasonID) {
        this.eventsThisGametick.add(new ChunkLog(x, z, d, event, stackTrace, reasonID));
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
        StackTraceDeobfuscator deobf = StackTraceDeobfuscator.create().withMinecraftVersion(CarpetSettings.minecraftVersion).withStableMcpNames(CarpetSettings.mcpMappings);
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
            if ((i < 0) || (i >= this.allTracesDeobfuscated.size())) {
                return null;
            } else {
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
            if (obfuscated == null) {
                return 0;
            }
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
            String deobfuscated = asString(trace, true);
            return this.internString(obfuscated, deobfuscated);
        }

        public int internReason() {
            return this.internString(reason);
        }

        private String asString(StackTraceElement[] trace, boolean deobfuscated) {
            if (deobfuscated) {
                trace = deobf.withStackTrace(trace).deobfuscate();
            }
            String stacktrace = new String();
            int i;
            int size = deobfuscated ? MAX_STACKTRACE_SIZE / 2 : MAX_STACKTRACE_SIZE;
            for (i = 0; i < trace.length && i < size; i++) {
                StackTraceElement e = trace[i];

                if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                    continue;
                }
                if (!stacktrace.isEmpty()) {
                    stacktrace += "\n";
                }
                stacktrace += e.toString();
            }
            if (size <= i && deobfuscated) {
                int reduce = trace.length - size;
                if (reduce > size) {
                    stacktrace += "\n.....cut out.....";
                    reduce = size;
                }
                for (i = trace.length - reduce; i < trace.length; i++) {
                    StackTraceElement e = trace[i];

                    if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                        continue;
                    }
                    if (!stacktrace.isEmpty()) {
                        stacktrace += "\n";
                    }
                    stacktrace += e.toString();
                }
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

        private HashMap<EntityPlayerMP, HashSet<Integer>> sentTracesForPlayer = new HashMap();

        public void registerPlayer(EntityPlayerMP sender, PacketBuffer data) {
            if (!CarpetSettings.chunkDebugTool) {
                CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_ACCESS_DENIED, new NBTTagCompound());
                return;
            }
            boolean addPlayer = data.readBoolean();
            if (addPlayer) {
                enabled = true;
                this.sentTracesForPlayer.put(sender, new HashSet<Integer>());
                this.sendInitalChunks(sender);
            } else {
                this.unregisterPlayer(sender);
            }
        }

        public void unregisterPlayer(EntityPlayerMP player) {
            sentTracesForPlayer.remove(player);
            if (sentTracesForPlayer.isEmpty()) {
                enabled = false;
            }
        }

        private void sendInitalChunks(EntityPlayerMP sender) {
            MinecraftServer server = sender.getServer();
            ArrayList<ChunkLog> logs = getInitialChunksForNewClient(server);
            sendMissingStackTracesForPlayer(sender, logs);
            for (int i = 0; i < logs.size(); i += LOGS_BATCH_SIZE) {
                boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
                List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
                NBTTagCompound chunkData = serializeEvents(batch, -server.getTickCounter() - 1, i, complete);
                if (chunkData != null) {
                    CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_EVENTS, chunkData);
                }
            }
        }

        private void sendUpdates() {
            if (this.sentTracesForPlayer.isEmpty()) {
                return;
            }

            ArrayList<ChunkLog> logs = getEventsThisGametick();
            MinecraftServer server = this.sentTracesForPlayer.keySet().iterator().next().server;

            for (EntityPlayerMP client : this.sentTracesForPlayer.keySet()) {
                this.sendMissingStackTracesForPlayer(client, logs);
            }
            for (int i = 0; i < logs.size(); i += LOGS_BATCH_SIZE) {
                boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
                List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
                NBTTagCompound chunkData = serializeEvents(batch, server.getTickCounter(), i, complete);
                if (chunkData != null) {
                    for (EntityPlayerMP player : this.sentTracesForPlayer.keySet()) {
                        CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_EVENTS, chunkData);
                    }
                }
            }
        }

        private void sendMissingStackTracesForPlayer(EntityPlayerMP player, ArrayList<ChunkLog> events) {
            HashSet<Integer> missingTraces = new HashSet<Integer>();
            HashSet<Integer> sentTraces = this.sentTracesForPlayer.getOrDefault(player, null);

            if (sentTraces == null) {
                return;
            }
            for (ChunkLog log : events) {
                int id = log.stackTraceIndex;
                int reason = log.reasonID;
                if (!sentTraces.contains(id)) {
                    sentTraces.add(id);
                    missingTraces.add(id);
                }
                if (!sentTraces.contains(reason)) {
                    sentTraces.add(reason);
                    missingTraces.add(reason);
                }
            }
            ArrayList missingList = new ArrayList(missingTraces);
            for (int i = 0; i < missingList.size(); i += STACKTRACES_BATCH_SIZE) {
                List<Integer> part = missingList.subList(i, Integer.min(i + STACKTRACES_BATCH_SIZE, missingList.size()));
                NBTTagCompound stackData = serializeStackTraces(part);
                if (stackData != null) {
                    CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_STACKTRACE, stackData);
                }
            }
        }

        private NBTTagCompound serializeEvents(List<ChunkLog> events, int gametick, int dataOffset, boolean complete) {
            if (events.isEmpty()) {
                return null;
            }
            NBTTagCompound chunkData = new NBTTagCompound();
            int data[] = new int[6 * events.size()];
            int i = 0;
            for (ChunkLog log : events) {
                data[i++] = log.chunkX;
                data[i++] = log.chunkZ;
                data[i++] = log.chunkDimension;
                data[i++] = log.event.ordinal();
                data[i++] = log.stackTraceIndex;
                data[i++] = log.reasonID;
            }
            chunkData.setInteger("size", events.size());
            chunkData.setIntArray("data", data);
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
                if (s == null) {
                    return null;
                }
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
            for (EntityPlayerMP player : this.sentTracesForPlayer.keySet()) {
                CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_ACCESS_DENIED, new NBTTagCompound());
            }
            this.sentTracesForPlayer.clear();
        }
    }
}