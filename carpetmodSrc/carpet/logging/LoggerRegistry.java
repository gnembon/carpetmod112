package carpet.logging;

import carpet.CarpetSettings;
import com.google.common.base.Charsets;
import com.google.gson.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoggerRegistry
{
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    // Map from logger names to loggers.
    private static Map<String, Logger> loggerRegistry = new HashMap<>();

    // List of default subscriptions
    private static Map<String, LoggerOptions> defaultSubscriptions = new HashMap<>();
    // Map from player names to the set of names of the logs that player is subscribed to.
    private static Map<String, Map<String, LoggerOptions>> playerSubscriptions = new HashMap<>();

    //statics to quickly asses if its worth even to call each one
    public static boolean __tnt;
    public static boolean __projectiles;
    public static boolean __fallingBlocks;
    public static boolean __kills;
    public static boolean __tps;
    public static boolean __counter;
    public static boolean __mobcaps;
    public static boolean __damage;
    public static boolean __packets;
    public static boolean __weather;
    public static boolean __tileTickLimit;
    public static boolean __portalCaching;
    public static boolean __instantComparators;
    public static boolean __items;
    public static boolean __rng;
    public static boolean __recipes;
    public static boolean __damageDebug;
    public static boolean __invisDebug;

    public static void initLoggers(MinecraftServer server)
    {
        registerLogger("tnt", new Logger(server, "tnt", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        registerLogger("projectiles", new Logger(server, "projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT));
        registerLogger("fallingBlocks",new Logger(server, "fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        registerLogger("kills", new Logger(server, "kills", null, null, LogHandler.CHAT));
        registerLogger("damage", new Logger(server, "damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT));
        registerLogger("weather", new Logger(server, "weather", null, null, LogHandler.CHAT));
        registerLogger("tileTickLimit", new Logger(server, "tileTickLimit", null, null, LogHandler.CHAT));
        registerLogger("portalCaching",new Logger(server, "portalCaching", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        registerLogger("instantComparators", new Logger(server, "instantComparators", "all", new String[]{"all", "tileTick", "buggy"}, LogHandler.CHAT));
        registerLogger("items",new Logger(server, "items", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        registerLogger("rng", new Logger(server, "rng", null, null, LogHandler.CHAT));

        registerLogger("tps", new Logger(server, "tps", null, null, LogHandler.HUD));
        registerLogger("packets", new Logger(server, "packets", null, null, LogHandler.HUD));
        registerLogger("counter",new Logger(server, "counter","white", new String[]{"cactus","white","orange","magenta","light_blue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"}, LogHandler.HUD));
        registerLogger("mobcaps", new Logger(server, "mobcaps", "dynamic",new String[]{"dynamic", "overworld", "nether","end"}, LogHandler.HUD));

        registerDebugger("recipes", new Logger(server, "recipes", null, null, LogHandler.CHAT));
        registerDebugger("damageDebug", new Logger(server, "damageDebug", null, null, LogHandler.CHAT));
        registerDebugger("invisDebug", new Logger(server, "invisDebug", null, null, LogHandler.CHAT));
    }

    private static File getSaveFile(MinecraftServer server) { return server.getActiveAnvilConverter().getFile(server.getFolderName(), "loggerData.json"); }

    public static void readSaveFile(MinecraftServer server) {
        File logData = getSaveFile(server);
        if (logData.isFile()) {
            try {
                JsonElement root = (new JsonParser()).parse(FileUtils.readFileToString(logData, Charsets.UTF_8));
                if (!root.isJsonObject())
                    return;

                JsonObject rootObj = root.getAsJsonObject();

                JsonArray defaultList = rootObj.getAsJsonArray("defaultList");
                for (JsonElement entryElement : defaultList) {
                    LoggerOptions options = new LoggerOptions();
                    options.fromJson(entryElement);

                    defaultSubscriptions.put(options.logger, options);
                }

                JsonObject playerList = rootObj.getAsJsonObject("players");
                for (Map.Entry<String, JsonElement> playerEntry : playerList.entrySet()) {
                    String username = playerEntry.getKey();
                    Map<String, LoggerOptions> subs = new HashMap<>();

                    JsonArray loggerEntries = playerEntry.getValue().getAsJsonArray();
                    for (JsonElement entryElement : loggerEntries) {
                        LoggerOptions options = new LoggerOptions();
                        options.fromJson(entryElement);

                        subs.put(options.logger, options);
                    }

                    playerSubscriptions.put(username, subs);
                }
            }
            catch (IOException ioexception)
            {
                LOGGER.error("Couldn't read default logger file {}", logData, ioexception);
            }
            catch (JsonParseException jsonparseexception)
            {
                LOGGER.error("Couldn't parse default logger file {}", logData, jsonparseexception);
            }
        }
    }

    public static void writeConf(MinecraftServer server) {
        File logData = getSaveFile(server);
        try
        {
            JsonObject root = new JsonObject();

            JsonArray defaultList = new JsonArray();
            for (Map.Entry<String, LoggerOptions> logger : defaultSubscriptions.entrySet()) {
                defaultList.add(logger.getValue().getSerializableElement());
            }
            root.add("defaultList", defaultList);

            JsonObject playerList = new JsonObject();
            for (Map.Entry<String, Map<String, LoggerOptions>> playerEntry : playerSubscriptions.entrySet()) {
                JsonArray playerLoggers = new JsonArray();

                for (LoggerOptions logger : playerEntry.getValue().values()) {
                    playerLoggers.add(logger.getSerializableElement());
                }

                playerList.add(playerEntry.getKey(), playerLoggers);
            }
            root.add("players", playerList);

            FileUtils.writeStringToFile(logData, root.toString(), Charsets.UTF_8);
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Couldn't save stats", (Throwable)ioexception);
        }
    }


    /**
     * Gets the default subscriptions
     */
    public static Map<String, LoggerOptions> getDefaultSubscriptions() { return defaultSubscriptions; }

    /**
     * Gets the logger with the given name. Returns null if no such logger exists.
     */
    public static Logger getLogger(String name) { return loggerRegistry.get(name); }

    /**
     * Gets the set of logger names.
     */
    public static String[] getLoggerNames(int debugger) { return loggerRegistry.entrySet().stream().filter(s -> s.getValue().debuggerFilter(debugger)).map(Map.Entry::getKey).toArray(String[]::new);}

    /**
     * Sets a log as a default log with the specified option and handler
     */
    public static boolean setDefault(MinecraftServer server, String logName, String option, LogHandler handler) {
        if (handler != null) {
            defaultSubscriptions.put(logName, new LoggerOptions(logName, option, handler.getName(), handler.getExtraArgs()));
        } else {
            defaultSubscriptions.put(logName, new LoggerOptions(logName, option, null));
        }
        writeConf(server);

        // Subscribe all players who have no customized subscription list
        for (EntityPlayer player : server.getPlayerList().getPlayers()) {
            if (!hasSubscriptions(player.getName()))
                subscribePlayer(player.getName(), logName, option, handler);
        }
        return true;
    }

    /**
     * Removes a log fro mthe list of default logs
     */
    public static boolean removeDefault(MinecraftServer server, String logName) {
        if (defaultSubscriptions.containsKey(logName)) {
            defaultSubscriptions.remove(logName);
            writeConf(server);

            // Unsubscribe all players who have no customized subscription list
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                if (!hasSubscriptions(player.getName()))
                    unsubscribePlayer(player.getName(), logName);
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a player is actively subscribed to anything
     */
    public static boolean hasSubscriptions(String playerName) { return playerSubscriptions.containsKey(playerName); }

    /**
     * Get the set of logs the current player is subscribed to.
     */
    public static Map<String, LoggerOptions> getPlayerSubscriptions(String playerName) {
        return playerSubscriptions.getOrDefault(playerName, new HashMap<>(defaultSubscriptions));
    }

    /**
     * Subscribes the player with name playerName to the log with name logName.
     */
    public static boolean subscribePlayer(MinecraftServer server, String playerName, String logName, String option, LogHandler handler)
    {
        if (!hasSubscriptions(playerName)) {
            playerSubscriptions.put(playerName, new HashMap<>(defaultSubscriptions));
        }

        Map<String, LoggerOptions> subs = getPlayerSubscriptions(playerName);
        if (!subs.containsKey(logName)) {
            if (handler != null) {
                subs.put(logName, new LoggerOptions(logName, option, handler.getName(), handler.getExtraArgs()));
            } else {
                subs.put(logName, new LoggerOptions(logName, option, null));
            }

            subscribePlayer(playerName, logName, option, handler);
            writeConf(server);
            return true;
        }
        return false;
    }

    /**
     * Unsubscribes the player with name playerName from the log with name logName.
     */
    public static boolean unsubscribePlayer(MinecraftServer server, String playerName, String logName)
    {
        if (!hasSubscriptions(playerName)) {
            playerSubscriptions.put(playerName, new HashMap<>(defaultSubscriptions));
        }

        Map<String, LoggerOptions> subs = getPlayerSubscriptions(playerName);
        if (subs.containsKey(logName)) {
            subs.remove(logName);
            unsubscribePlayer(playerName, logName);
            writeConf(server);
            return true;
        }
        return false;
    }

    /**
     * If the player is not subscribed to the log, then subscribe them. Otherwise, unsubscribe them.
     */
    public static boolean togglePlayerSubscription(MinecraftServer server, String playerName, String logName, LogHandler handler)
    {
        if (getPlayerSubscriptions(playerName).containsKey(logName))
        {
            unsubscribePlayer(server, playerName, logName);
            return false;
        }
        else
        {
            subscribePlayer(server, playerName, logName, null, handler);
            return true;
        }
    }

    /**
     * Unsubscribes a player from all logs and removes the subscription entry
     * This restores the default subscriptions
     */
    public static void resetSubscriptions(MinecraftServer server, String playerName) {
        Map<String, LoggerOptions> subs = getPlayerSubscriptions(playerName);

        // Unsubscribe from all subscriptions
        for (String logName : subs.keySet()) {
            unsubscribePlayer(playerName, logName);
        }

        if (hasSubscriptions(playerName))
            playerSubscriptions.remove(playerName);

        writeConf(server);

        // Restore default subscriptions
        for (LoggerOptions option : defaultSubscriptions.values()) {
            LogHandler handler = null;
            if (option.handlerName != null)
                handler = LogHandler.createHandler(option.handlerName, option.extraArgs);

            subscribePlayer(playerName, option.logger, option.option, handler);
        }
    }

    protected static void setAccess(Logger logger)
    {
        String name = logger.getLogName();
        boolean value = logger.hasSubscribers();
        try
        {
            Field f = LoggerRegistry.class.getDeclaredField("__"+name);
            f.setBoolean(null, value);
        }
        catch (IllegalAccessException e)
        {
            CarpetSettings.LOG.error("Cannot change logger quick access field");
        }
        catch (NoSuchFieldException e)
        {
            CarpetSettings.LOG.error("Wrong logger name");
        }
    }
    /**
     * Called when the server starts. Creates the logs used by Carpet mod.
     */
    private static void registerLogger(String name, Logger logger)
    {
        loggerRegistry.put(name, logger);
        setAccess(logger);
    }
    /**
     * Used to register runtime debugging logger.
     */
    private static void registerDebugger(String recipes, Logger recipes1) {
        registerLogger(recipes, recipes1.asDebugger());
    }

    public static void playerConnected(EntityPlayer player)
    {
        String playerName = player.getName();

        Map<String, LoggerOptions> subs = getPlayerSubscriptions(playerName);
        for (LoggerOptions option : subs.values()) {
            LogHandler handler = null;
            if (option.handlerName != null)
                handler = LogHandler.createHandler(option.handlerName, option.extraArgs);

            subscribePlayer(playerName, option.logger, option.option, handler);
        }
    }
    public static void playerDisconnected(EntityPlayer player)
    {
        String playerName = player.getName();

        for (String logName : LoggerRegistry.getLoggerNames(0)) {
            unsubscribePlayer(playerName, logName);
        }
    }

    // ===== PRIVATE FUNCTIONS TO PREVENT CODE DUPLICATION ===== //
    private static void subscribePlayer(String playerName, String logName, String option, LogHandler handler) {
        carpet.logging.Logger log = LoggerRegistry.getLogger(logName);
        if(log == null) return;

        if (option == null)
            option = log.getDefault();

        log.addPlayer(playerName, option, handler);
    }

    private static void unsubscribePlayer(String playerName, String logName) {
        LoggerRegistry.getLogger(logName).removePlayer(playerName);
    }
}
