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
import carpet.utils.LRUCache;
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
    StackTraces stackTraces = new StackTraces();
    private final ChunkLoggerSerializer clients = new ChunkLoggerSerializer();
    private final ArrayList<ChunkLog> eventsThisGametick = new ArrayList<>();
    public static String reason = null;
    public static String oldReason = null;

    private static final int MAX_STACKTRACE_SIZE = 60;

    public enum Event {
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
        final InternedString stackTrace;
        final InternedString reason;

        ChunkLog(int x, int z, int d, Event e, InternedString trace, InternedString reason) {
            this.chunkX = x;
            this.chunkZ = z;
            this.chunkDimension = d;
            this.event = e;
            this.stackTrace = trace;
            this.reason = reason;
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

    /*
     * main logging function
     * logs a change in a chunk including a stacktrace if required by the client
     */
    public void log(World w, int x, int z, Event e) {
        log(x, z, getWorldIndex(w), e, stackTraces.internStackTrace(), stackTraces.internReason());
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
        ArrayList<ChunkLog> forNewClient = new ArrayList<>();
        int dimension = -1;
        for (World w : server.worlds) {
            ChunkProviderServer provider = (ChunkProviderServer) (w.getChunkProvider());
            dimension++;
            for (Chunk c : provider.getLoadedChunks()) {
                forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.LOADING, null, null));
                if (provider.isChunkUnloadScheduled(c.x, c.z)) {
                    forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.QUEUE_UNLOAD, null, null));
                    if (!c.unloadQueued) {
                        forNewClient.add(new ChunkLog(c.x, c.z, dimension, Event.CANCEL_UNLOAD, null, null));
                    }
                }
            }
            PlayerChunkMap chunkmap = ((WorldServer) w).getPlayerChunkMap();
            Iterator<ChunkPos> i = chunkmap.carpetGetAllChunkCoordinates();
            while (i.hasNext()) {
                ChunkPos pos = i.next();
                forNewClient.add(new ChunkLog(pos.x, pos.z, dimension, Event.PLAYER_ENTERS, null, null));
            }
        }
        return forNewClient;
    }

    private ArrayList<ChunkLog> getEventsThisGametick() {
        return this.eventsThisGametick;
    }

    private void log(int x, int z, int d, Event event, InternedString stackTrace, InternedString reasonID) {
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

    private static class InternedString {
        public final String obfuscated;
        public final String deobfuscated;
        public final int id;

        public InternedString(int id, String obfuscated, String deobfuscated) {
            this.id = id;
            this.obfuscated = obfuscated;
            this.deobfuscated = deobfuscated;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InternedString that = (InternedString) o;
            return id == that.id && Objects.equals(obfuscated, that.obfuscated) && Objects.equals(deobfuscated, that.deobfuscated);
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class StackTraces {
        private static final StackTraceDeobfuscator DEOBFUSCATOR = StackTraceDeobfuscator.create().withMinecraftVersion(CarpetSettings.minecraftVersion).withStableMcpNames(CarpetSettings.mcpMappings);
        private final Map<String, InternedString> internedStrings = new LRUCache<>(128); // 64 ~ 98%, 128+ > 99%
        private int nextId = 1;

        private InternedString internString(String obfuscated, String deobfuscated) {
            if (obfuscated == null) return null;
            InternedString internedString = internedStrings.get(obfuscated);
            if (internedString == null) {
                internedString = new InternedString(nextId++, obfuscated, deobfuscated);
                internedStrings.put(obfuscated, internedString);
            }
            return internedString;
        }

        private InternedString internString(String s) {
            return this.internString(s, s);
        }

        private InternedString internStackTrace() {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            String obfuscated = asString(trace, false);
            String deobfuscated = asString(trace, true);
            return this.internString(obfuscated, deobfuscated);
        }

        public InternedString internReason() {
            return this.internString(reason);
        }

        private String asString(StackTraceElement[] trace, boolean deobfuscated) {
            if (deobfuscated) {
                trace = DEOBFUSCATOR.withStackTrace(trace).deobfuscate();
            }
            StringBuilder stacktrace = new StringBuilder();
            int i;
            int size = deobfuscated ? MAX_STACKTRACE_SIZE / 2 : MAX_STACKTRACE_SIZE;
            for (i = 0; i < trace.length && i < size; i++) {
                StackTraceElement e = trace[i];

                if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                    continue;
                }
                if (stacktrace.length() > 0) {
                    stacktrace.append("\n");
                }
                stacktrace.append(e.toString());
            }
            if (size <= i && deobfuscated) {
                int reduce = trace.length - size;
                if (reduce > size) {
                    stacktrace.append("\n.....cut out.....");
                    reduce = size;
                }
                for (i = trace.length - reduce; i < trace.length; i++) {
                    StackTraceElement e = trace[i];

                    if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                        continue;
                    }
                    if (stacktrace.length() > 0) {
                        stacktrace.append("\n");
                    }
                    stacktrace.append(e.toString());
                }
            }
            return stacktrace.toString();
        }
    }

    private class ChunkLoggerSerializer {

        private static final int PACKET_EVENTS = 0;
        private static final int PACKET_STACKTRACE = 1;
        private static final int PACKET_ACCESS_DENIED = 2;

        private static final int STACKTRACES_BATCH_SIZE = 10;
        private static final int LOGS_BATCH_SIZE = 1000;

        private final Map<EntityPlayerMP, HashSet<InternedString>> sentTracesForPlayer = new WeakHashMap<>();

        public void registerPlayer(EntityPlayerMP sender, PacketBuffer data) {
            if (!CarpetSettings.chunkDebugTool) {
                CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_ACCESS_DENIED, new NBTTagCompound());
                return;
            }
            boolean addPlayer = data.readBoolean();
            if (addPlayer) {
                enabled = true;
                this.sentTracesForPlayer.put(sender, new HashSet<>());
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
            HashSet<InternedString> missingTraces = new HashSet<>();
            HashSet<InternedString> sentTraces = this.sentTracesForPlayer.getOrDefault(player, null);

            if (sentTraces == null) {
                return;
            }
            for (ChunkLog log : events) {
                InternedString stackTrace = log.stackTrace;
                InternedString reason = log.reason;
                if (stackTrace != null && !sentTraces.contains(stackTrace)) {
                    sentTraces.add(stackTrace);
                    missingTraces.add(stackTrace);
                }
                if (reason != null && !sentTraces.contains(reason)) {
                    sentTraces.add(reason);
                    missingTraces.add(reason);
                }
            }
            ArrayList<InternedString> missingList = new ArrayList<>(missingTraces);
            for (int i = 0; i < missingList.size(); i += STACKTRACES_BATCH_SIZE) {
                List<InternedString> part = missingList.subList(i, Integer.min(i + STACKTRACES_BATCH_SIZE, missingList.size()));
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
            int[] data = new int[6 * events.size()];
            int i = 0;
            for (ChunkLog log : events) {
                data[i++] = log.chunkX;
                data[i++] = log.chunkZ;
                data[i++] = log.chunkDimension;
                data[i++] = log.event.ordinal();
                data[i++] = log.stackTrace == null ? 0 : log.stackTrace.id;
                data[i++] = log.reason == null ? 0 : log.reason.id;
            }
            chunkData.setInteger("size", events.size());
            chunkData.setIntArray("data", data);
            chunkData.setInteger("offset", dataOffset);
            chunkData.setInteger("time", gametick);
            chunkData.setBoolean("complete", complete);
            return chunkData;
        }

        private NBTTagCompound serializeStackTraces(List<InternedString> strings) {
            if (strings.isEmpty()) {
                return null;
            }
            NBTTagList list = new NBTTagList();
            for (InternedString obfuscated : strings) {
                NBTTagCompound stackTrace = new NBTTagCompound();
                stackTrace.setInteger("id", obfuscated.id);
                stackTrace.setString("stack", obfuscated.deobfuscated);
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