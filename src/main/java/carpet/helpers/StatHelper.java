package carpet.helpers;

import carpet.mixin.accessors.ScoreCriteriaStatAccessor;
import carpet.mixin.accessors.StatCraftingAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.class_5569;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.class_2590;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;
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
    private static Map<UUID, StatHandler> cache;
    private static long cacheTime;
    private static final Stat[] BLOCK_STATE_STATS = new Stat[256 * 16];
    private static final Int2ObjectMap<Stat> CRAFT_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECT_USE_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECTS_PICKED_UP_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECTS_DROPPED_META_STATS = new Int2ObjectOpenHashMap<>();

    public static File[] getStatFiles(MinecraftServer server) {
        File statsDir = new File(server.getWorldById(0).method_25960().method_28318(), "stats");
        return statsDir.listFiles((dir, name) -> name.endsWith(".json"));
    }

    public static Map<UUID, StatHandler> getAllStatistics(MinecraftServer server) {
        if (cache != null && server.getTicks() - cacheTime < 100) return cache;
        File[] files = getStatFiles(server);
        HashMap<UUID, StatHandler> stats = new HashMap<>();
        PlayerManager players = server.getPlayerManager();
        for (File file : files) {
            String filename = file.getName();
            String uuidString = filename.substring(0, filename.lastIndexOf(".json"));
            try {
                UUID uuid = UUID.fromString(uuidString);
                ServerPlayerEntity player = players.getPlayer(uuid);
                if (player != null) {
                    stats.put(uuid, players.createStatHandler(player));
                } else {
                    ServerStatHandler manager = new ServerStatHandler(server, file);
                    manager.method_33862();
                    stats.put(uuid, manager);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        cache = stats;
        cacheTime = server.getTicks();
        return stats;
    }

    @Nullable
    public static String getUsername(MinecraftServer server, UUID uuid) {
        UserCache profileCache = server.getUserCache();
        GameProfile profile = profileCache.getByUuid(uuid);
        if (profile != null) return profile.getName();
        MinecraftSessionService sessionService = server.getSessionService();
        profile = sessionService.fillProfileProperties(new GameProfile(uuid, null), false);
        if (profile.isComplete()) return profile.getName();
        LOGGER.warn("Could not find name of user " + uuid);
        return null;
    }

    public static void initialize(Scoreboard scoreboard, MinecraftServer server, ScoreboardObjective objective) {
        LOGGER.info("Initializing " + objective);
        ScoreboardCriterions criteria = objective.getDisplayName();
        if (!(criteria instanceof class_5569)) return;
        Stat stat = ((ScoreCriteriaStatAccessor) criteria).getStat();
        for (Map.Entry<UUID, StatHandler> statEntry : getAllStatistics(server).entrySet()) {
            StatHandler stats = statEntry.getValue();
            int value = stats.method_33903(stat);
            if (value == 0) continue;
            String username = getUsername(server, statEntry.getKey());
            if (username == null) continue;
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(username, objective);
            score.setScore(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }

    public static Stat getBlockStateStats(BlockState state) {
        Block block = state.getBlock();
        int id = Block.getId(block);
        int meta = block.getMeta(state);
        return BLOCK_STATE_STATS[(id << 4) | meta];
    }

    public static Stat getCraftStats(Item item, int meta) {
        int id = Item.getRawId(item);
        return CRAFT_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getObjectUseStats(Item item, int meta) {
        int id = Item.getRawId(item);
        return OBJECT_USE_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getObjectsPickedUpStats(Item item, int meta) {
        int id = Item.getRawId(item);
        return OBJECTS_PICKED_UP_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getDroppedObjectStats(Item item, int meta) {
        int id = Item.getRawId(item);
        return OBJECTS_DROPPED_META_STATS.get((id << 4) | (meta & 0xf));
    }

    private interface StatStorage {
        void store(int stateId, Stat stat);
    }

    private static void registerSubStats(class_2590 baseStat, StatStorage storage, Function<Text, TranslatableText> textFun) {
        Item item = ((StatCraftingAccessor) baseStat).getItem();
        int id = Item.getRawId(item);
        if (item.hasVariants()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                Text text = textFun.apply(stackWithMeta.toHoverableText());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).method_33869();
                storage.store(stateId, statWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                storage.store(stateId, baseStat);
            }
        }
    }

    public static void addCraftStats(class_2590 baseStat) {
        registerSubStats(baseStat, CRAFT_META_STATS::put, text -> new TranslatableText("stat.craftItem", text));
    }

    public static void addMineStats(class_2590 baseStat) {
        registerSubStats(baseStat, (state, stat) -> BLOCK_STATE_STATS[state] = stat, text -> new TranslatableText("stat.mineBlock", text));
    }

    public static void addUseStats(class_2590 baseStat) {
        registerSubStats(baseStat, OBJECT_USE_META_STATS::put, text -> new TranslatableText("stat.useItem", text));
    }

    public static void addPickedUpStats(class_2590 baseStat) {
        registerSubStats(baseStat, OBJECTS_PICKED_UP_META_STATS::put, text -> new TranslatableText("stat.pickup", text));
    }

    public static void addDroppedStats(class_2590 baseStat) {
        registerSubStats(baseStat, OBJECTS_DROPPED_META_STATS::put, text -> new TranslatableText("stat.drop", text));
    }
}
