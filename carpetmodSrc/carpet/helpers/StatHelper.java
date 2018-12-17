package carpet.helpers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.StatisticsManagerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<UUID, StatisticsManager> cache;
    private static long cacheTime;

    public static File[] getStatFiles(MinecraftServer server) {
        File statsDir = new File(server.getWorld(0).getSaveHandler().getWorldDirectory(), "stats");
        return statsDir.listFiles((dir, name) -> name.endsWith(".json"));
    }

    public static Map<UUID, StatisticsManager> getAllStatistics(MinecraftServer server) {
        if (cache != null && server.getTickCounter() - cacheTime < 100) return cache;
        File[] files = getStatFiles(server);
        HashMap<UUID, StatisticsManager> stats = new HashMap<>();
        PlayerList players = server.getPlayerList();
        for (File file : files) {
            String filename = file.getName();
            String uuidString = filename.substring(0, filename.lastIndexOf(".json"));
            try {
                UUID uuid = UUID.fromString(uuidString);
                EntityPlayerMP player = players.getPlayerByUUID(uuid);
                if (player != null) {
                    stats.put(uuid, players.getPlayerStatsFile(player));
                } else {
                    StatisticsManagerServer manager = new StatisticsManagerServer(server, file);
                    manager.readStatFile();
                    stats.put(uuid, manager);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        cache = stats;
        cacheTime = server.getTickCounter();
        return stats;
    }

    @Nullable
    public static String getUsername(MinecraftServer server, UUID uuid) {
        PlayerProfileCache profileCache = server.getPlayerProfileCache();
        GameProfile profile = profileCache.getProfileByUUID(uuid);
        if (profile != null) return profile.getName();
        MinecraftSessionService sessionService = server.getMinecraftSessionService();
        profile = sessionService.fillProfileProperties(new GameProfile(uuid, null), false);
        if (profile.isComplete()) return profile.getName();
        LOGGER.warn("Could not find name of user " + uuid);
        return null;
    }

    public static void initialize(Scoreboard scoreboard, MinecraftServer server, ScoreObjective objective) {
        LOGGER.info("Initializing " + objective);
        IScoreCriteria criteria = objective.getCriteria();
        if (!(criteria instanceof ScoreCriteriaStat)) return;
        StatBase stat = ((ScoreCriteriaStat) criteria).stat;
        for (Map.Entry<UUID, StatisticsManager> statEntry : getAllStatistics(server).entrySet()) {
            StatisticsManager stats = statEntry.getValue();
            int value = stats.readStat(stat);
            if (value == 0) continue;
            String username = getUsername(server, statEntry.getKey());
            if (username == null) continue;
            Score score = scoreboard.getOrCreateScore(username, objective);
            score.setScorePoints(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }
}
