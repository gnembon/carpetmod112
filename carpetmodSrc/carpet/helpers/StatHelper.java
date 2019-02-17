package carpet.helpers;

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

    public static void addCraftStats(Item item, StatCrafting baseStat) {
        int id = Item.getIdFromItem(item);
        if (item.getHasSubtypes()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                ITextComponent text = new TextComponentTranslation("stat.craftItem", stackWithMeta.getTextComponent());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).registerStat();
                CRAFT_META_STATS.put(stateId, statWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                CRAFT_META_STATS.put(stateId, baseStat);
            }
        }
    }

    public static void addMineStats(Block block, StatCrafting baseStat) {
        Item item = Item.getItemFromBlock(block);
        int id = Item.getIdFromItem(item);
        if (item.getHasSubtypes()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(block, 1, meta);
                ITextComponent text = new TextComponentTranslation("stat.mineBlock", stackWithMeta.getTextComponent());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).registerStat();
                BLOCK_STATE_STATS[stateId] = statWithMeta;
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                BLOCK_STATE_STATS[stateId] = baseStat;
            }
        }
    }

    public static void addUseStats(Item item, StatCrafting baseStat) {
        int id = Item.getIdFromItem(item);
        if (item.getHasSubtypes()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                ITextComponent text = new TextComponentTranslation("stat.useItem", stackWithMeta.getTextComponent());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).registerStat();
                OBJECT_USE_META_STATS.put(stateId, statWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                OBJECT_USE_META_STATS.put(stateId, baseStat);
            }
        }
    }

    public static void addPickedUpAndDroppedStats(Item item, StatCrafting basePickupStat, StatCrafting baseDropStat) {
        int id = Item.getIdFromItem(item);
        if (item.getHasSubtypes()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                ITextComponent textPickup = new TextComponentTranslation("stat.pickup", stackWithMeta.getTextComponent());
                StatSubItem pickupWithMeta = (StatSubItem) new StatSubItem(basePickupStat, meta, textPickup).registerStat();
                OBJECTS_PICKED_UP_META_STATS.put(stateId, pickupWithMeta);
                ITextComponent textDrop = new TextComponentTranslation("stat.drop", stackWithMeta.getTextComponent());
                StatSubItem dropWithMeta = (StatSubItem) new StatSubItem(baseDropStat, meta, textDrop).registerStat();
                OBJECTS_DROPPED_META_STATS.put(stateId, dropWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                OBJECTS_PICKED_UP_META_STATS.put(stateId, basePickupStat);
                OBJECTS_DROPPED_META_STATS.put(stateId, baseDropStat);
            }
        }
    }
}
