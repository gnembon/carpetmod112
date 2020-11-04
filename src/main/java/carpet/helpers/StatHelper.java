package carpet.helpers;

import carpet.mixin.accessors.ScoreCriteriaStatAccessor;
import carpet.mixin.accessors.StatCraftingAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class StatHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<UUID, StatisticsManager> cache;
    private static long cacheTime;
    private static final StatBase[] BLOCK_STATE_STATS = new StatBase[256 * 16];
    private static final Int2ObjectMap<StatBase> CRAFT_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<StatBase> OBJECT_USE_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<StatBase> OBJECTS_PICKED_UP_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<StatBase> OBJECTS_DROPPED_META_STATS = new Int2ObjectOpenHashMap<>();

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
        StatBase stat = ((ScoreCriteriaStatAccessor) criteria).getStat();
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

    public static StatBase getBlockStateStats(IBlockState state) {
        Block block = state.getBlock();
        int id = Block.getIdFromBlock(block);
        int meta = block.getMetaFromState(state);
        return BLOCK_STATE_STATS[(id << 4) | meta];
    }

    public static StatBase getCraftStats(Item item, int meta) {
        int id = Item.getIdFromItem(item);
        return CRAFT_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static StatBase getObjectUseStats(Item item, int meta) {
        int id = Item.getIdFromItem(item);
        return OBJECT_USE_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static StatBase getObjectsPickedUpStats(Item item, int meta) {
        int id = Item.getIdFromItem(item);
        return OBJECTS_PICKED_UP_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static StatBase getDroppedObjectStats(Item item, int meta) {
        int id = Item.getIdFromItem(item);
        return OBJECTS_DROPPED_META_STATS.get((id << 4) | (meta & 0xf));
    }

    private interface StatStorage {
        void store(int stateId, StatBase stat);
    }

    private static void registerSubStats(StatCrafting baseStat, StatStorage storage, Function<ITextComponent, TextComponentTranslation> textFun) {
        Item item = ((StatCraftingAccessor) baseStat).getItem();
        int id = Item.getIdFromItem(item);
        if (item.getHasSubtypes()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                ITextComponent text = textFun.apply(stackWithMeta.getTextComponent());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).registerStat();
                storage.store(stateId, statWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                storage.store(stateId, baseStat);
            }
        }
    }

    public static void addCraftStats(StatCrafting baseStat) {
        registerSubStats(baseStat, CRAFT_META_STATS::put, text -> new TextComponentTranslation("stat.craftItem", text));
    }

    public static void addMineStats(StatCrafting baseStat) {
        registerSubStats(baseStat, (state, stat) -> BLOCK_STATE_STATS[state] = stat, text -> new TextComponentTranslation("stat.mineBlock", text));
    }

    public static void addUseStats(StatCrafting baseStat) {
        registerSubStats(baseStat, OBJECT_USE_META_STATS::put, text -> new TextComponentTranslation("stat.useItem", text));
    }

    public static void addPickedUpStats(StatCrafting baseStat) {
        registerSubStats(baseStat, OBJECTS_PICKED_UP_META_STATS::put, text -> new TextComponentTranslation("stat.pickup", text));
    }

    public static void addDroppedStats(StatCrafting baseStat) {
        registerSubStats(baseStat, OBJECTS_DROPPED_META_STATS::put, text -> new TextComponentTranslation("stat.drop", text));
    }
}
