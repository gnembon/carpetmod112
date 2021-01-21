package carpet;

import carpet.helpers.StackTraceDeobfuscator;
import carpet.network.PluginChannelManager;
import carpet.network.ToggleableChannelHandler;
import carpet.patches.FakeServerPlayerEntity;
import carpet.pubsub.*;
import carpet.utils.*;
import carpet.utils.extensions.WaypointContainer;
import carpet.worldedit.WorldEditBridge;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import narcolepticfrog.rsmm.events.TickStartEventDispatcher;
import narcolepticfrog.rsmm.server.RSMMServer;

import carpet.carpetclient.CarpetClientServer;

import carpet.helpers.TickSpeed;
import carpet.logging.LoggerRegistry;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class CarpetServer // static for now - easier to handle all around the code, its one anyways
{
    public static final Random rand = new Random((int)((2>>16)*Math.random()));
    public static final PubSubManager PUBSUB = new PubSubManager();
    public static final PubSubMessenger PUBSUB_MESSENGER = new PubSubMessenger(PUBSUB);

    private static final Logger LOGGER = LogManager.getLogger("Carpet|Server");
    private static final CompletableFuture<StackTraceDeobfuscator> DEOBFUSCATOR = StackTraceDeobfuscator.loadDefault();

    public static MinecraftServer minecraft_server;
    public static PluginChannelManager pluginChannels;
    public static RSMMServer rsmmServer;
    public static ToggleableChannelHandler rsmmChannel;
    public static ToggleableChannelHandler wecuiChannel;
    public static ThreadLocal<Boolean> playerInventoryStacking = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static CarpetClientServer CCServer;

    public static void init(MinecraftServer server) //aka constructor of this static singleton class
    {
        minecraft_server = server;
        pluginChannels = new PluginChannelManager(server);
        pluginChannels.register(PUBSUB_MESSENGER);

        CCServer = new CarpetClientServer(server);
        pluginChannels.register(CCServer);

        rsmmServer = new RSMMServer(server);
        rsmmChannel = new ToggleableChannelHandler(pluginChannels, rsmmServer.createChannelHandler(), false);
        wecuiChannel = new ToggleableChannelHandler(pluginChannels, WorldEditBridge.createChannelHandler(), false);
    }

    public static void onServerLoaded(MinecraftServer server) {
        CarpetSettings.applySettingsFromConf(server);
        LoggerRegistry.initLoggers(server);
        LoggerRegistry.readSaveFile(server);
        WorldEditBridge.onServerLoaded(server);
    }

    public static void onLoadAllWorlds(MinecraftServer server)
    {
        TickingArea.loadConfig(server);
        for (ServerWorld world : server.worlds) {
            int dim = world.dimension.getType().getRawId();
            try {
                ((WaypointContainer) world).setWaypoints(Waypoint.loadWaypoints(world));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            String prefix = "minecraft." + world.dimension.getType().method_27531();
            new PubSubInfoProvider<>(PUBSUB,prefix + ".chunk_loading.dropped_chunks.hash_size",20,
                    () -> ChunkLoading.getCurrentHashSize(world));
            for (SpawnGroup creatureType : SpawnGroup.values()) {
                String mobCapPrefix = prefix + ".mob_cap." + creatureType.name().toLowerCase(Locale.ROOT);
                new PubSubInfoProvider<>(PUBSUB, mobCapPrefix + ".filled", 20, () -> {
                    Pair<Integer, Integer> mobCap = SpawnReporter.mobcaps.get(dim).get(creatureType);
                    if (mobCap == null) return 0;
                    return mobCap.getLeft();
                });
                new PubSubInfoProvider<>(PUBSUB, mobCapPrefix + ".total", 20, () -> {
                    Pair<Integer, Integer> mobCap = SpawnReporter.mobcaps.get(dim).get(creatureType);
                    if (mobCap == null) return 0;
                    return mobCap.getRight();
                });
            }
        }
    }
    public static void onWorldsSaved(MinecraftServer server)
    {
        TickingArea.saveConfig(server);
        for (ServerWorld world : server.worlds) {
            try {
                Waypoint.saveWaypoints(world, ((WaypointContainer) world).getWaypoints());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static void tick(MinecraftServer server)
    {
        TickSpeed.tick(server);
        if (CarpetSettings.redstoneMultimeter)
        {
            TickStartEventDispatcher.dispatchEvent(server.getTicks());
        }
        HUDController.update_hud(server);
        WorldEditBridge.onStartTick();
        PUBSUB.update(server.getTicks());
    }
    public static void playerConnected(ServerPlayerEntity player)
    {
        pluginChannels.onPlayerConnected(player);
        LoggerRegistry.playerConnected(player);
    }

    public static void playerDisconnected(ServerPlayerEntity player)
    {
        pluginChannels.onPlayerDisconnected(player);
        LoggerRegistry.playerDisconnected(player);
    }
    
    public static Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_)
    {
        long i = (long)p_72843_1_ * 341873128712L + (long)p_72843_2_ * 132897987541L + CCServer.getMinecraftServer().worlds[0].getLevelProperties().method_28225() + (long)p_72843_3_;
        rand.setSeed(i);
        return rand;
    }

    public static void loadBots(MinecraftServer server) {
        try
        {
            File settings_file = server.getLevelStorage().method_28330(server.getLevelName(), "bot.conf");
            BufferedReader b = new BufferedReader(new FileReader(settings_file));
            String line = "";
            boolean temp = CarpetSettings.removeFakePlayerSkins;
            CarpetSettings.removeFakePlayerSkins = true;
            while ((line = b.readLine()) != null)
            {
                FakeServerPlayerEntity.create(line, server);
            }
            b.close();
            CarpetSettings.removeFakePlayerSkins = temp;
        }
        catch(FileNotFoundException e)
        {
            System.out.println(e);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    public static void writeConf(MinecraftServer server, ArrayList<String> names)
    {
        try
        {
            File settings_file = server.getLevelStorage().method_28330(server.getLevelName(), "bot.conf");
            if(names != null) {
                FileWriter fw = new FileWriter(settings_file);
                for (String name : names) {
                    fw.write(name +"\n");
                }
                fw.close();
            } else {
                settings_file.delete();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Nullable
    public static StackTraceDeobfuscator getDeobfuscator(boolean block) {
        if (!DEOBFUSCATOR.isDone() && !block) return null;
        if (DEOBFUSCATOR.isCompletedExceptionally() || DEOBFUSCATOR.isCancelled()) return null;
        try {
            return DEOBFUSCATOR.join();
        } catch (RuntimeException e) {
            LOGGER.debug(e);
            return null;
        }
    }
}
