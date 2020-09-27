package carpet;

import carpet.commands.*;
import carpet.helpers.StackTraceDeobfuscator;
import carpet.network.PluginChannelManager;
import carpet.network.ToggleableChannelHandler;
import carpet.patches.EntityPlayerMPFake;
import carpet.pubsub.*;
import carpet.utils.*;
import carpet.utils.extensions.WaypointContainer;
import carpet.worldedit.WorldEditBridge;

import java.io.*;
import java.util.*;

import narcolepticfrog.rsmm.events.TickStartEventDispatcher;
import narcolepticfrog.rsmm.server.RSMMServer;

import carpet.carpetclient.CarpetClientServer;

import carpet.helpers.TickSpeed;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import carpet.logging.LoggerRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;

public class CarpetServer // static for now - easier to handle all around the code, its one anyways
{
    public static final Random rand = new Random((int)((2>>16)*Math.random()));
    public static final PubSubManager PUBSUB = new PubSubManager();
    public static final PubSubMessenger PUBSUB_MESSENGER = new PubSubMessenger(PUBSUB);

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
    public static void onServerLoaded(MinecraftServer server)
    {
        CarpetSettings.applySettingsFromConf(server);
        LoggerRegistry.initLoggers(server);
        LoggerRegistry.readSaveFile(server);
        WorldEditBridge.onServerLoaded(server);

        // Precache mappings so as not to lag the server later
        StackTraceDeobfuscator.create()
                .withMinecraftVersion(CarpetSettings.minecraftVersion)
                .withSnapshotMcpNames(CarpetSettings.mcpMappings)
                .withStackTrace(new StackTraceElement[0])
                .deobfuscate();
    }
    public static void onLoadAllWorlds(MinecraftServer server)
    {
        TickingArea.loadConfig(server);
        for (WorldServer world : server.worlds) {
            int dim = world.provider.getDimensionType().getId();
            try {
                ((WaypointContainer) world).setWaypoints(Waypoint.loadWaypoints(world));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            String prefix = "minecraft." + world.provider.getDimensionType().getName();
            new PubSubInfoProvider<>(PUBSUB,prefix + ".chunk_loading.dropped_chunks.hash_size",20,
                    () -> ChunkLoading.getCurrentHashSize(world));
            for (EnumCreatureType creatureType : EnumCreatureType.values()) {
                String mobCapPrefix = prefix + ".mob_cap." + creatureType.name().toLowerCase(Locale.ROOT);
                new PubSubInfoProvider<>(PUBSUB, mobCapPrefix + ".filled", 20, () -> {
                    Tuple<Integer, Integer> mobCap = SpawnReporter.mobcaps.get(dim).get(creatureType);
                    if (mobCap == null) return 0;
                    return mobCap.getFirst();
                });
                new PubSubInfoProvider<>(PUBSUB, mobCapPrefix + ".total", 20, () -> {
                    Tuple<Integer, Integer> mobCap = SpawnReporter.mobcaps.get(dim).get(creatureType);
                    if (mobCap == null) return 0;
                    return mobCap.getSecond();
                });
            }
        }
    }
    public static void onWorldsSaved(MinecraftServer server)
    {
        TickingArea.saveConfig(server);
        for (WorldServer world : server.worlds) {
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
            TickStartEventDispatcher.dispatchEvent(server.getTickCounter());
        }
        HUDController.update_hud(server);
        WorldEditBridge.onStartTick();
        PUBSUB.update(server.getTickCounter());
    }
    public static void playerConnected(EntityPlayerMP player)
    {
        pluginChannels.onPlayerConnected(player);
        LoggerRegistry.playerConnected(player);
    }

    public static void playerDisconnected(EntityPlayerMP player)
    {
        pluginChannels.onPlayerDisconnected(player);
        LoggerRegistry.playerDisconnected(player);
    }
    
    public static Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_)
    {
        long i = (long)p_72843_1_ * 341873128712L + (long)p_72843_2_ * 132897987541L + CCServer.getMinecraftServer().worlds[0].getWorldInfo().getSeed() + (long)p_72843_3_;
        rand.setSeed(i);
        return rand;
    }

    public static void loadBots(MinecraftServer server) {
        try
        {
            File settings_file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "bot.conf");
            BufferedReader b = new BufferedReader(new FileReader(settings_file));
            String line = "";
            boolean temp = CarpetSettings.removeFakePlayerSkins;
            CarpetSettings.removeFakePlayerSkins = true;
            while ((line = b.readLine()) != null)
            {
                EntityPlayerMPFake.create(line, server);
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
            File settings_file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "bot.conf");
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

    public static void registerCommands(ServerCommandManager mgr) {
        mgr.registerCommand(new CommandEntityInfo());
        mgr.registerCommand(new CommandSpawn());
        mgr.registerCommand(new CommandTick());
        mgr.registerCommand(new CommandProfile());
        mgr.registerCommand(new CommandDistance());
        mgr.registerCommand(new CommandBlockInfo());
        mgr.registerCommand(new CommandCarpet());
        mgr.registerCommand(new CommandCounter());
        mgr.registerCommand(new CommandUnload());
        mgr.registerCommand(new CommandUnload13());
        mgr.registerCommand(new CommandGMC());
        mgr.registerCommand(new CommandGMS());
        mgr.registerCommand(new CommandLog());
        mgr.registerCommand(new CommandDebuglogger());
        mgr.registerCommand(new CommandDebugCarpet());
        mgr.registerCommand(new CommandSubscribe());
        mgr.registerCommand(new CommandPerimeter());
        mgr.registerCommand(new CommandRNG());
        mgr.registerCommand(new CommandTickingArea());
        mgr.registerCommand(new CommandStructure());
        mgr.registerCommand(new CommandFillBiome());
        mgr.registerCommand(new CommandPing());
        mgr.registerCommand(new CommandRemoveEntity());
        mgr.registerCommand(new CommandGrow());
        mgr.registerCommand(new CommandScoreboardPublic());
        mgr.registerCommand(new CommandUpdateCarpet());
        mgr.registerCommand(new CommandLagSpike());
        mgr.registerCommand(new CommandPlayer());
        mgr.registerCommand(new CommandAutosave());
        mgr.registerCommand(new CommandWaypoint());
        mgr.registerCommand(new CommandTNT());
        mgr.registerCommand(new CommandLight());

        rsmmServer.registerCommands(mgr);
    }
}
