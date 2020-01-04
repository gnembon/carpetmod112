package carpet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.stream.Collectors;

import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.carpetclient.CarpetClientRuleChanger;
import carpet.helpers.RandomTickOptimization;
import carpet.helpers.ScoreboardDelta;
import carpet.patches.BlockWool;
import carpet.utils.TickingArea;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;

import static carpet.CarpetSettings.RuleCategory.*;

public class CarpetSettings
{
    public static boolean locked = false;

    // TODO: replace these constants at build time
    public static final String carpetVersion = "v20_01_01";
    public static final String minecraftVersion = "1.12.2";
    public static final String mcpMappings = "39-1.12";

    public static final Logger LOG = LogManager.getLogger();

    public static long setSeed = 0; // Xcom: if you dunno where to put it, shove it in CarpetSettings - Earth :)

    // ===== COMMANDS ===== //
    /*
     * Rules in this category should start with the "command" prefix
     */

    @Rule(desc = "Enables /spawn command for spawn tracking", category = COMMANDS)
    public static boolean commandSpawn = true;

    @Rule(desc = "Enables /tick command to control game speed", category = COMMANDS)
    public static boolean commandTick = true;

    @Rule(desc = "Enables /log command to monitor events in the game via chat and overlays", category = COMMANDS)
    public static boolean commandLog = true;

    @Rule(desc = "Enables /distance command to measure in game distance between points", category = COMMANDS, extra = {
            "Also enables brown carpet placement action if 'carpets' rule is turned on as well"
    })
    public static boolean commandDistance = true;

    @Rule(desc = "Enables /blockinfo command", category = COMMANDS, extra = {
            "Also enables gray carpet placement action if 'carpets' rule is turned on as well"
    })
    public static boolean commandBlockInfo = true;

    @Rule(desc = "Enables /entityinfo command", category = COMMANDS, extra = {
            "Also enables yellow carpet placement action if 'carpets' rule is turned on as well"
    })
    public static boolean commandEntityInfo = true;

    @Rule(desc = "Enables /unload command to inspect chunk unloading order", category = COMMANDS)
    public static boolean commandUnload = true;

    @Rule(desc = "Enables /c and /s commands to quickly switch between camera and survival modes", category = COMMANDS, extra = {
            "/c and /s commands are available to all players regardless of their permission levels"
    })
    public static boolean commandCameramode = true;

    @Rule(desc = "Enables /perimeterinfo command that scans the area around the block for potential spawnable spots", category = COMMANDS)
    public static boolean commandPerimeterInfo = true;

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMANDS)
    public static boolean commandPlayer = true;

    @Rule(desc = "Enables /rng command to manipulate and query rng", category = COMMANDS)
    public static boolean commandRNG = true;

    @Rule(desc = "Enables /structure to manage NBT structures used by structure blocks", category = COMMANDS)
    public static boolean commandStructure = true;

    @Rule(desc = "Enables /fillbiome command to change the biome of an area", category = COMMANDS)
    public static boolean commandFillBiome = true;

    @Rule(desc = "Enables /autosave command to query information about the autosave and execute commands relative to the autosave", category = COMMANDS)
    public static boolean commandAutosave = true;

    @Rule(desc = "Enables /ping for players to get their ping", category = COMMANDS)
    public static boolean commandPing = true;

    @Rule(desc = "Enables /waypoint for saving coordinates", category = COMMANDS)
    public static boolean commandWaypoint = true;

    @Rule(desc = "Disables players in /c from spectating other players", category = COMMANDS)
    public static boolean cameraModeDisableSpectatePlayers;

    @Rule(desc = "Places players back to the original location when using camera mode by using /c then /s", category = COMMANDS)
    public static boolean cameraModeRestoreLocation;

    // ===== CREATIVE TOOLS ===== //

    @Rule(desc = "Sets the instant falling flag to true. The boolean used in world population that can be exploited turning true making falling blocks fall instantly.", category = CREATIVE, validator = "validateInstantFallingFlag")
    public static boolean instantFallingFlag = false;

    private static boolean validateInstantFallingFlag(boolean value) {
        if (value) {
            BlockFalling.fallInstantly = true;
        }else {
            BlockFalling.fallInstantly = false;
        }
        return true;
    }

    @Rule(desc = "Sets the instant scheduled updates instantly to true. The boolean used in world population that can be exploited turning true making all repeaters, comperators, observers and similar components update instantly.", category = CREATIVE, validator = "validateInstantScheduling")
    public static boolean instantScheduling = false;
    private static boolean validateInstantScheduling(boolean value) {
        if (value) {
            for (int dim = 0; dim < 3; dim++) {
                WorldServer world = CarpetServer.minecraft_server.worlds[dim];
                world.scheduledUpdatesAreImmediate = true;
            }
        }else {
            for (int dim = 0; dim < 3; dim++) {
                WorldServer world = CarpetServer.minecraft_server.worlds[dim];
                world.scheduledUpdatesAreImmediate = false;
            }
        }
        return true;
    }

    @Rule(desc = "Quasi Connectivity doesn't require block updates.", category = EXPERIMENTAL, extra = {
            "All redstone components will send extra block updates downwards",
            "Affects hoppers, droppers and dispensers"
    })
    public static boolean extendedConnectivity = false;

    @Rule(desc = "Portals won't let a creative player go through instantly", category = CREATIVE, extra = {
            "Holding obsidian in either hand won't let you through at all"
    })
    @CreativeDefault
    public static boolean portalCreativeDelay = false;

    @Rule(desc = "Players absorb XP instantly, without delay", category = CREATIVE)
    public static boolean xpNoCooldown = false;

    @Rule(desc = "XP orbs combine with other into bigger orbs", category = CREATIVE)
    public static boolean combineXPOrbs = false;

    @Rule(desc = "Pumpkins and fence gates can be placed in mid air", category = CREATIVE, extra = "Needs carpet client. Fixed in 1.13")
    public static boolean relaxedBlockPlacement = false;

    @Rule(desc = "Explosions won't destroy blocks", category = TNT)
    public static boolean explosionNoBlockDamage = false;

    @Rule(desc = "Removes random TNT momentum when primed", category = TNT)
    public static boolean tntPrimerMomentumRemoved = false;

    @Rule(desc = "Enables controlable TNT jump angle RNG for debuging.", category = TNT)
    public static boolean TNTAdjustableRandomAngle;

    @Rule(desc = "Allows to place blocks in different orientations. Requires Carpet Client", category = CREATIVE, extra = {
            "Also prevents rotations upon placement of dispensers and furnaces",
            "when placed into a world by commands"
    })
    public static boolean accurateBlockPlacement = false;

    @Rule(desc = "Repeater pointing from and to wool blocks transfer signals wirelessly", category = CREATIVE, validator = "validateWirelessRedstone", extra = {
            "Temporary feature - repeaters need an update when reloaded",
            "By Narcoleptic Frog"
    })
    public static boolean wirelessRedstone = false;

    private static boolean validateWirelessRedstone(boolean value) {
        if (!value)
            ((BlockWool) Blocks.WOOL).clearWirelessLocations();
        return true;
    }

    @Rule(desc = "Repeater delays depends on stained hardened clay aka terracotta on which they are placed", category = {EXPERIMENTAL, CREATIVE}, extra = {
            "1 to 15 gt per delay added (1-15 block data), 0 (white) adds 100gt per tick"
    })
    public static boolean repeaterPoweredTerracotta = false;

    @Rule(desc = "TNT doesn't update when placed against a power source", category = TNT)
    public static boolean TNTDoNotUpdate = false;

    @Rule(desc = "Pistons, droppers and dispensers react if block above them is powered", category = CREATIVE)
    public static boolean quasiConnectivity = true;

    @Rule(desc = "Players can flip and rotate blocks when holding cactus", category = {CREATIVE, SURVIVAL}, extra = {
            "Doesn't cause block updates when rotated/flipped",
            "Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc..."
    })
    @CreativeDefault
    @SurvivalDefault
    public static boolean flippinCactus = false;

    @Rule(desc = "hoppers pointing to wool will count items passing through them", category = {COMMANDS, CREATIVE, SURVIVAL}, extra = {
            "Enables /counter command, and actions while placing red and green carpets on wool blocks",
            "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
            "In survival, place green carpet on same color wool to query, red to reset the counters",
            "Counters are global and shared between players, 16 channels available",
            "Items counted are destroyed, count up to one stack per tick per hopper"
    })
    @CreativeDefault
    @SurvivalDefault
    public static boolean hopperCounters = false;

    @Rule(desc = "Enables integration with redstone multimeter mod", category = {CREATIVE, SURVIVAL}, validator = "validateRedstoneMultimeter", extra = {
            "Required clients with RSMM Mod by Narcoleptic Frog. Enables multiplayer experience with RSMM Mod"
    })
    public static boolean redstoneMultimeter = false;

    private static boolean validateRedstoneMultimeter(boolean value) {
        CarpetServer.rsmmChannel.setEnabled(value);
        return true;
    }

    @Rule(desc = "Pistons can push tile entities, like hoppers, chests etc.", category = EXPERIMENTAL)
    public static boolean movableTileEntities = false;

    @Rule(desc = "Uses nametags to display current mobs AI tasks", category = CREATIVE)
    public static boolean displayMobAI = false;

    @Rule(name = "noteBlockImitationOf1.13", desc = "Note blocks have update capabilities behaviour from 1.13", category = EXPERIMENTAL)
    public static boolean noteBlockImitationOf1_13 = false;

    @Rule(desc = "summoning a lightning bolt has all the side effects of natural lightning", category = CREATIVE)
    public static boolean summonNaturalLightning = false;

    @Rule(name = "1.8Spawning", desc = "Using old 1.8 spawning rules: always 4 mobs per pack and honoring entity collisions while spawning", category = EXPERIMENTAL)
    public static boolean _1_8Spawning = false;

    @Rule(desc = "Reintroduces piston warping/translocation bug", category = EXPERIMENTAL)
    public static boolean pocketPushing = false;

    @Rule(desc = "Observers don't pulse when placed", category = CREATIVE)
    public static boolean observersDoNonUpdate = false;

    @Rule(desc = "Transparent observers, TNT and redstone blocks. May cause lighting artifacts", category = CREATIVE, validator = "validateFlyingMachineTransparent")
    public static boolean flyingMachineTransparent = false;

    @Rule(desc = "Structure blocks remove entities in the bounding box when load entity option is enabled.", category = CREATIVE)
    public static boolean structuresReplaceEntities = false;

    @Rule(desc = "Allows to always be able to eat cakes.", category = CREATIVE)
    public static boolean cakeAlwaysEat;

    private static boolean validateFlyingMachineTransparent(boolean value) {
        int newOpacity = value ? 0 : 255;
        Blocks.OBSERVER.setLightOpacity(newOpacity);
        Blocks.REDSTONE_BLOCK.setLightOpacity(newOpacity);
        Blocks.TNT.setLightOpacity(newOpacity);
        return true;
    }

    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
    @CreativeDefault("false")
    public static boolean fillUpdates = true;

    @Rule(desc = "Customizable piston push limit", category = CREATIVE, options = {"10", "12", "14", "100"}, validator = "validateNonNegative")
    public static int pushLimit = 12;

    @Rule(desc = "Customizable powered rail power range", category = CREATIVE, options = {"9", "15", "30"}, validator = "validatePositive")
    public static int railPowerLimit = 9;

    @Rule(desc = "Customizable fill/clone volume limit", category = CREATIVE, options = {"32768", "250000", "1000000"}, validator = "validateNonNegative")
    @CreativeDefault("500000")
    public static int fillLimit = 32768;

    @Rule(desc = "Sets the horizontal random angle on TNT for debugging of TNT contraptions", category = TNT, options = "-1", validator = "validateHardcodeTNTangle", extra = {
            "Set to -1 for default behaviour"
    })
    public static double hardcodeTNTangle = -1;
    private static boolean validateHardcodeTNTangle(double value) {
        return value == -1 || (value >= 0 && value < 360);
    }

    @Rule(desc = "Sets the tnt random explosion range to a fixed value", category = TNT, options = "-1", validator = "validateTntRandomRange", extra = {
            "Set to -1 for default behaviour"
    })
    public static double tntRandomRange = -1;
    private static boolean validateTntRandomRange(double value) {
        return value == -1 || value >= 0;
    }

    @Rule(desc = "Sets a different motd message on client trying to connect to the server", category = CREATIVE, options = "_", extra = {
            "use '_' to use the startup setting from server.properties"
    })
    public static String customMOTD = "_";

    @Rule(desc = "1.8 double retraction from pistons.", category = EXPERIMENTAL, extra = {
            "Gives pistons the ability to double retract without side effects."
    })
    public static boolean doubleRetraction = false;

    @Rule(desc = "Turning nether RNG manipulation on or off.", category = CREATIVE)
    public static boolean netherRNG = false;

    @Rule(desc = "Turning end RNG manipulation on or off.", category = CREATIVE)
    public static boolean endRNG = false;

    @Rule(desc = "Changes the view distance of the server.", category = CREATIVE, options = {"0", "12", "16", "32", "64"}, validator = "validateViewDistance", extra = {
            "Set to 0 to not override the value in server settings."
    })
    public static int viewDistance = 0;
    private static boolean validateViewDistance(int value) {
        if (value != 0 && (value < 2 || value > 64))
            return false;
        if (value == 0)
            value = ((DedicatedServer) CarpetServer.minecraft_server).getIntProperty("view-distance", 10);
        if (value != CarpetServer.minecraft_server.getPlayerList().getViewDistance())
            CarpetServer.minecraft_server.getPlayerList().setViewDistance(value);
        return true;
    }

    @Rule(desc = "Enable use of ticking areas.", category = {CREATIVE, EXPERIMENTAL}, validator = "validateTickingAreas", extra = {
            "As set by the /tickingarea command.",
            "Ticking areas work as if they are the spawn chunks."
    })
    public static boolean tickingAreas = false;
    private static boolean validateTickingAreas(boolean value) {
        if (value && CarpetServer.minecraft_server.worlds != null)
            TickingArea.initialChunkLoad(CarpetServer.minecraft_server, false);
        return true;
    }

    @Rule(desc = "Removes the spawn chunks.", category = CREATIVE, validator = "validateDisableSpawnChunks")
    public static boolean disableSpawnChunks = false;
    private static boolean validateDisableSpawnChunks(boolean value) {
        if (!value && CarpetServer.minecraft_server.worlds != null) {
            World overworld = CarpetServer.minecraft_server.worlds[0];
            for (ChunkPos chunk : new TickingArea.SpawnChunks().listIncludedChunks(overworld))
                overworld.getChunkProvider().provideChunk(chunk.x, chunk.z);
        }
        return true;
    }

    @Rule(desc = "Changes the structure block dimension limit.", category = CREATIVE, options = {"32", "50", "200", "1000"}, validator = "validateNonNegative")
    public static int structureBlockLimit = 32;

    @Rule(desc = "Enables chunk debug on carpet client.", category = CREATIVE, validator = "validateChunkDebugTool")
    public static boolean chunkDebugTool = false;
    private static boolean validateChunkDebugTool(boolean value) {
        if (!value)
            CarpetClientChunkLogger.logger.disable();
        return true;
    }

    @Rule(desc = "Enables/disables WorldEdit.", category = {CREATIVE, EXPERIMENTAL}, validator = "validateWorldEdit", extra = {
            "Only works in WorldEdit is in the classpath."
    })
    @CreativeDefault
    public static boolean worldEdit = false;
    private static boolean validateWorldEdit(boolean value) {
        CarpetServer.wecuiChannel.setEnabled(value && WorldEditBridge.worldEditPresent);
        return true;
    }

    @Rule(desc = "Disables player entity collision.", category = {CREATIVE, EXPERIMENTAL})
    public static boolean disablePlayerCollision = false;

    @Rule(desc = "Enables randomtick indexing on carpet client.", category = {CREATIVE})
    public static boolean randomtickingChunkUpdates = false;

    @Rule(desc = "Disables snow, ice and lightning in nether and end for stable LCG.", category = {CREATIVE})
    public static boolean enableStableLCGNetherEnd = false;

    @Rule(desc = "Enable creative player no-clip.", category = {CREATIVE})
    public static boolean creativeNoClip = false;

    @Rule(desc = "Allows players to place blocks inside entity's.", category = {CREATIVE})
    public static boolean ignoreEntityWhenPlacing = false;

    public static enum WhereToChunkSavestate {
        unload(false), everywhere_except_players(true), everywhere(true);
        public final boolean canUnloadNearPlayers;
        WhereToChunkSavestate(boolean canUnloadNearPlayers) {
            this.canUnloadNearPlayers = canUnloadNearPlayers;
        }
    }

    @Rule(desc = "Where chunk savestating is allowed to happen", category = CREATIVE)
    public static WhereToChunkSavestate whereToChunkSavestate = WhereToChunkSavestate.unload;

    @Rule(desc = "When true, the game acts as if a permaloader is running", category = CREATIVE)
    public static boolean simulatePermaloader = false;

    // ===== FIXES ===== //
    /*
     * Rules in this category should end with the "Fix" suffix
     */

    @Rule(desc = "Fixes the speed los on entitys after reload.", category = FIX)
    public static boolean reloadEntitySpeedlossFix;

    @Rule(desc = "Disables the packet limit that causes the book banning.", category = FIX)
    public static boolean disableBookBan;

    @Rule(desc = "Rule made to debug recipes by pasting all recipes when crafting.", category = FIX)
    public static boolean debugRecipes;

    @Rule(desc = "Fixes the collision cancelation lag when mobs are inside ladders and vines.", category = FIX)
    @BugFixDefault
    public static boolean optimizedCollisionCancellations = false;

    @Rule(desc = "Nether portals correctly place entities going through", category = FIX, extra = {
            "Entities shouldn't suffocate in obsidian"
    })
    @BugFixDefault
    public static boolean portalSuffocationFix = false;

    @Rule(desc = "Remove ghost blocks when mining too fast", category = FIX, extra = "Fixed in 1.13")
    @SurvivalDefault
    public static boolean miningGhostBlocksFix = false;

    @Rule(desc = "Nether portals won't teleport you on occasion to 8x coordinates", category = FIX, extra = {
            "It also prevents from taking random fire damage when going through portals"
    })
    @BugFixDefault
    public static boolean portalTeleportationFix = false;

    @Rule(desc = "Redstone torches respond correctly to 2 tick pulses", category = FIX, extra = "Fixed in 1.13")
    @BugFixDefault
    public static boolean inconsistentRedstoneTorchesFix = false;

    @Rule(desc = "Prevents llamas from taking player food while breeding", category = FIX)
    @BugFixDefault
    public static boolean llamaOverfeedingFix = false;

    @Rule(desc = "Guardians, ghasts, blazes... honor players' invisibility effect", category = FIX)
    @BugFixDefault
    public static boolean invisibilityFix = false;

    @Rule(desc = "Allows mobs with potion effects to despawn outside of player range", category = FIX, extra = {
            "Specifically effective to let witches drinking their own stuffs despawn"
    })
    @BugFixDefault
    public static boolean potionsDespawnFix = false;

    @Rule(desc = "Prevents players from mounting animals when holding breeding food", category = FIX)
    @BugFixDefault
    public static boolean breedingMountingDisabled = false;

    @Rule(desc = "Mobs growing up won't glitch into walls or go through fences", category = FIX)
    @BugFixDefault
    public static boolean growingUpWallJumpFix = false;

    @Rule(desc = "Won't let mobs glitch into blocks when reloaded.", category = {FIX, EXPERIMENTAL}, validator = "validateReloadSuffocationFix", extra = {
            "Can cause slight differences in mobs behaviour"
    })
    @BugFixDefault
    public static boolean reloadSuffocationFix = false;
    private static boolean validateReloadSuffocationFix(boolean value) {
        if (value)
            AxisAlignedBB.margin = 1.0 / (1L << 27);
        else
            AxisAlignedBB.margin = 0;
        return true;
    }

    @Rule(desc = "Redstone dust algorithm", category = {EXPERIMENTAL, OPTIMIZATIONS}, extra = {
            "Fast redstone dust by Theosib",
            "Random redstone dust to test if your contraption is locational"
    })
    public static RedstoneDustAlgorithm redstoneDustAlgorithm = RedstoneDustAlgorithm.vanilla;
    public static enum RedstoneDustAlgorithm {
        vanilla, fast, random
    }

    @Rule(desc = "TNT causes less lag when exploding in the same spot and in liquids", category = TNT)
    public static boolean optimizedTNT = false;

    @Rule(desc = "Fixes server crashing under heavy load and low tps", category = FIX, extra = {
            "Won't prevent crashes if the server doesn't respond in max-tick-time ticks"
    })
    @BugFixDefault
    public static boolean watchdogFix = false;

    @Rule(desc = "Reduces the lag caused by tile entities.", category = EXPERIMENTAL, extra = "By PallaPalla")
    public static boolean optimizedTileEntities = false;

    @Rule(desc = "Merges stationary primed TNT entities", category = TNT)
    public static boolean mergeTNT = false;

    @Rule(desc = "Entities pushed or moved into unloaded chunks no longer disappear", category = {EXPERIMENTAL, CREATIVE})
    @BugFixDefault
    public static boolean unloadedEntityFix = false;

    @Rule(desc = "Prevents players from rubberbanding when moving too fast", category = {CREATIVE, SURVIVAL})
    @CreativeDefault
    public static boolean antiCheatSpeed = false;

    @Rule(desc = "Spawned mobs that would otherwise despawn immediately, won't be placed in world", category = OPTIMIZATIONS)
    public static boolean optimizedDespawnRange = false;

    @Rule(desc = "Optimized movement calculation or very fast moving entities", category = EXPERIMENTAL)
    public static boolean fastMovingEntityOptimization = false;

    @Rule(desc = "Optimized entity-block collision calculations. By masa", category = EXPERIMENTAL)
    public static boolean blockCollisionsOptimization = false;

    @Rule(desc = "Structure bounding boxes (i.e. witch huts) will generate correctly", category = FIX, extra = {
            "Fixes spawning issues due to incorrect bounding boxes"
    })
    public static boolean boundingBoxFix = false;

    @Rule(desc = "Blocks inherit the original light opacity and light values while being pushed with a piston", category = OPTIMIZATIONS)
    public static boolean movingBlockLightOptimization = false;

    @Rule(desc = "Chunk saving issues that causes entites and blocks to duplicate or disappear", category = FIX, extra = "By Theosib")
    @BugFixDefault
    public static boolean entityDuplicationFix = false;

    @Rule(desc = "Fixes duplication of items when using item frames", category = FIX)
    public static boolean duplicationFixItemFrame = false;

    @Rule(desc = "Fixes duplication of items when using gravity blocks through portals", category = FIX)
    public static boolean duplicationFixGravityBlocks = false;

    @Rule(desc = "Fixes duplication of items when entitys enter end portals and die the same time", category = FIX)
    public static boolean duplicationFixPortalEntitys = false;

    @Rule(desc = "Fixes duplication of TNT when pushed by pistons", category = FIX)
    public static boolean duplicationFixMovingTNT = false;

    @Rule(desc = "Fixes duplication of rails when pushed by pistons", category = FIX)
    public static boolean duplicationFixMovingRail = false;

    @Rule(desc = "Fixes duplication of carpets when pushed by pistons", category = FIX)
    public static boolean duplicationFixMovingCarpets = false;

    @Rule(desc = "Fixes duplication of items when players drop items on the ground and log out the same time", category = FIX)
    public static boolean duplicationFixLogout = false;

    @Rule(desc = "Fixes duplication of entitys when players log out riding entitys in unloaded chunks", category = FIX)
    public static boolean duplicationFixRidingEntitys = false;

    @Rule(desc = "Fixes duplication of blocks when using update suppression", category = FIX)
    public static boolean duplicationFixUpdateSuppression = false;

    @Rule(desc = "Uses alternative lighting engine by PhiPros. AKA NewLight mod", category = OPTIMIZATIONS)
    public static boolean newLight = false;

    @Rule(desc = "Permanent fires don't schedule random updates", category = EXPERIMENTAL)
    @BugFixDefault
    public static boolean calmNetherFires = false;

    @Rule(desc = "Customizable maximal entity collision limits, 0 for no limits", category = OPTIMIZATIONS, options = {"0", "1", "20"}, validator = "validateNonNegative")
    public static int maxEntityCollisions = 0;

    @Rule(
            desc = "Fix for piston ghost blocks. 'clientAndServer' option requires carpet-client", category = FIX,
            extra = "Does not work properly on vanilla clients with non-vanilla push limits"
    )
    @BugFixDefault
    public static PistonGhostBlocksFix pistonGhostBlocksFix = PistonGhostBlocksFix.off;
    public static enum PistonGhostBlocksFix
    {
        off, serverOnly, clientAndServer
    }

    @Rule(desc = "fixes water flowing issues", category = OPTIMIZATIONS)
    public static WaterFlow waterFlow = WaterFlow.vanilla;
    public static enum WaterFlow {
        vanilla, optimized, correct
    }

    @Rule(desc = "Fixes bug with piston serialization", category = FIX)
    @BugFixDefault
    public static boolean pistonSerializationFix = false;

    @Rule(desc = "Fixes reload update order for tile entities", category = FIX, extra = {
            "Fixes instant wires randomly breaking.",
            "Effective after chunk reload."
    })
    @BugFixDefault
    public static boolean reloadUpdateOrderFix = false;

    @Rule(desc = "Fixes to leashes.", category = FIX)
    @BugFixDefault
    public static LeashFix leashFix = LeashFix.off;
    public static enum LeashFix {
        off, casual, cool
    }

    @Rule(desc = "Stops blocks which don't need to be random ticked from being random ticked", category = FIX, validator = "validateRandomTickOptimization", extra = "Fixed in 1.13")
    @BugFixDefault
    public static boolean randomTickOptimization = false;
    private static boolean validateRandomTickOptimization(boolean value) {
        RandomTickOptimization.setUselessRandomTicks(!value);
        RandomTickOptimization.recalculateAllChunks();
        return true;
    }

    @Rule(desc = "Fix dismount behavior that leads to ghost chicken jockeys", category = FIX)
    @BugFixDefault
    public static boolean dismountFix = false;

    @Rule(desc = "Disables the catching-up behavior after lag spikes", category = FIX)
    @BugFixDefault
    public static boolean disableVanillaTickWarp = false;

    @Rule(desc = "Fixes chunk updates for players riding minecarts or llamas", category = FIX)
    @BugFixDefault
    public static boolean ridingPlayerUpdateFix = false;

    @Rule(desc = "Fixes players clipping through moving piston blocks partially.", category = FIX, options = {"0", "20", "40", "100"}, validator = "validatePistonClippingFix")
    public static int pistonClippingFix = 0;
    private static boolean validatePistonClippingFix(int pistonClippingFix) {
        // TODO
        return true;
    }

    @Rule(desc = "Recovers potion effects when they were replaced and the replacement ended", category = FIX)
    @BugFixDefault
    public static boolean effectsFix = false;

    @Rule(desc = "Fixes entity tracker not rendering entitys such as players in minecarts or boats.", category = FIX)
    public static boolean entityTrackerFix;

    @Rule(desc = "Players go invisible after using portals.", category = FIX)
    public static boolean portalTurningPlayersInvisibleFix;

    @Rule(desc = "Fixes updates suppression causing server crashes.", category = FIX)
    public static boolean updateSuppressionCrashFix;

    @Rule(desc = "Fixes double tile tick scheduling", category = FIX, validator = "validateDoubleTileTickSchedulingFix")
    public static boolean doubleTileTickSchedulingFix = false;
    private static boolean validateDoubleTileTickSchedulingFix(boolean value) {
        if (CarpetServer.minecraft_server.worlds == null)
            return true;
        @SuppressWarnings("unchecked")
        ArrayList<NextTickListEntry>[] tileTicks = new ArrayList[3];
        for (int dim = 0; dim < 3; dim++) {
            WorldServer world = CarpetServer.minecraft_server.worlds[dim];
            tileTicks[dim] = new ArrayList<>(world.pendingTickListEntriesHashSet);
            world.pendingTickListEntriesHashSet.clear();
            world.pendingTickListEntriesTreeSet.clear();
        }
        doubleTileTickSchedulingFix = value; // set this early
        for (int dim = 0; dim < 3; dim++) {
            WorldServer world = CarpetServer.minecraft_server.worlds[dim];
            world.pendingTickListEntriesHashSet.addAll(tileTicks[dim]);
            world.pendingTickListEntriesTreeSet.addAll(tileTicks[dim]);
        }
        return true;
    }

    @Rule(desc = "Fixes player position truncation causing chunks to load with one block offset to chunk boarders in negative coordinates.", category = FIX)
    public static boolean playerChunkLoadingFix = false;

    // ===== FEATURES ===== //

    @Rule(desc = "Turns crafting tables into automated crafting tables with inventorys.", category = FEATURE, extra = "WARNING! If the rule is turned off after use, any inventory content in crafting tables will permanently become lost after chunks are reloaded.")
    public static boolean autocrafter;

    @Rule(desc = "Scoreboard displays changes over time, specified in seconds.", options = {"0", "60", "600", "3600"}, validator = "validateScoreboardDelta", category = EXPERIMENTAL, extra = {
            "Set to 0 to disable Scoreboard delta display."
    })
    public static int scoreboardDelta = 0;
    private static boolean validateScoreboardDelta(int value) {
        if(value == 0) {
            scoreboardDelta = 0;
            ScoreboardDelta.resetScoreboardDelta();
        }
        return true;
    }

    @Rule(desc = "Dropping entire stacks works also from on the crafting UI result slot", category = {FIX, SURVIVAL})
    @SurvivalDefault
    public static boolean ctrlQCraftingFix = false;

    @Rule(desc = "Liquids don't ignore random tick updates", category = FEATURE, validator = "validateLiquidsNotRandom", extra = "Removed in 1.13")
    @BugFixDefault
    public static boolean liquidsNotRandom = false;
    private static boolean validateLiquidsNotRandom(boolean value) {
        RandomTickOptimization.setLiquidRandomTicks(!value);
        RandomTickOptimization.recalculateAllChunks();
        return true;
    }

    @Rule(desc = "Mobs no longer can control minecarts", category = FEATURE, extra = "Removed in 1.13")
    @BugFixDefault
    public static boolean mobsDontControlMinecarts = false;

    @Rule(desc = "Parrots don't get of your shoulders until you receive damage", category = {SURVIVAL, FEATURE})
    @SurvivalDefault
    public static boolean persistentParrots = false;

    @Rule(desc = "Wet sponges dry in the nether dimension", category = FEATURE)
    public static boolean spongesDryInTheNether = false;

    @Rule(desc = "Empty shulker boxes can stack to 64 when dropped on the ground", category = SURVIVAL, extra = {
            "To move them around between inventories, use shift click to move entire stacks"
    })
    @SurvivalDefault
    public static boolean stackableEmptyShulkerBoxes = false;

    @Rule(desc = "Named ghasts won't attack players and allow to be ridden and controlled", category = {SURVIVAL, FEATURE}, extra = {
            "Hold a ghast tear to bring a tamed ghast close to you",
            "Use fire charges when riding to shoot fireballs",
            "Requires flying to be enabled on the server"
    })
    public static boolean rideableGhasts = false;

    @Rule(desc = "Only husks spawn in desert temples", category = {EXPERIMENTAL, FEATURE})
    public static boolean huskSpawningInTemples = false;

    @Rule(desc = "Shulkers will respawn in end cities", category = {FEATURE, EXPERIMENTAL}, validator = "validateShulkerSpawningInEndCities")
    public static boolean shulkerSpawningInEndCities = false;
    private static boolean validateShulkerSpawningInEndCities(boolean value) {
        net.minecraft.world.gen.structure.MapGenEndCity.shulkerSpawning(value);
        return true;
    }

    @Rule(desc = "Guardians turn into Elder Guardian when struck by lightning", category = {EXPERIMENTAL, FEATURE})
    public static boolean renewableElderGuardians = false;

    @Rule(desc = "Saplings turn into dead shrubs in hot climates and no water access when it attempts to grow into a tree", category = FEATURE)
    public static boolean desertShrubs = false;

    @Rule(desc = "Nitwit villagers will have 3 hidden crafting recipes they can craft", category = EXPERIMENTAL, extra = {
            "They require food for crafting and prefer a specific food type to craft faster.",
            "They have one crafting recipe to start out and unlock there higher recipes as they craft",
            "The nitwits will craft faster as they progress",
            "When a crafting table is nearby they will throw the product towards it",
            "They need a crafting table to craft tier 2 and higher recipes"
    })
    public static boolean nitwitCrafter = false;

    @Rule(desc = "Villagers automaticaly trade from items on the ground", category = EXPERIMENTAL)
    public static boolean villagerAutoTrader;

    @Rule(desc = "Silverfish drop a gravel item when breaking out of a block", category = EXPERIMENTAL)
    public static boolean silverFishDropGravel = false;

    @Rule(desc = "Multiple ice crushed by falling anvils make packed ice", category = EXPERIMENTAL)
    public static boolean renewablePackedIce = false;

    @Rule(desc = "Dragon eggs when fed meet items spawn more eggs", category = EXPERIMENTAL)
    public static boolean renewableDragonEggs = false;

    @Rule(desc = "Placing carpets may issue carpet commands for non-op players", category = SURVIVAL)
    @SurvivalDefault
    public static boolean carpets = false;

    @Rule(desc = "Pistons, Glass and Sponge can be broken faster with their appropriate tools", category = SURVIVAL)
    @SurvivalDefault
    public static boolean missingTools = false;

    @Rule(desc = "Alternative caching strategy for nether portals", category = {SURVIVAL, EXPERIMENTAL})
    @CreativeDefault
    @SurvivalDefault
    public static boolean portalCaching = false;

    @Rule(desc = "The percentage of required sleeping players to skip the night", category = EXPERIMENTAL, options = {"0", "10", "50", "100"}, validator = "validateSleepingThreshold", extra = {
            "Use values from 0 to 100, 100 for default (all players needed)"
    })
    public static int sleepingThreshold = 100;
    private static boolean validateSleepingThreshold(int value) {
        return value >= 0 && value <= 100;
    }

    @Rule(desc = "sponge responds to random ticks", category = {EXPERIMENTAL, FEATURE}, validator = "validateSpongeRandom")
    public static boolean spongeRandom = false;
    private static boolean validateSpongeRandom(boolean value) {
        RandomTickOptimization.setSpongeRandomTicks(value);
        RandomTickOptimization.recalculateAllChunks();
        return true;
    }

    @Rule(desc = "Cactus in dispensers rotates blocks.", category = EXPERIMENTAL, extra = {
            "Cactus in a dispenser gives the dispenser the ability to rotate the blocks that are in front of it anti-clockwise if possible."
    })
    public static boolean rotatorBlock = false;

    @Rule(desc = "Water bottles in dispensers fill with water when dispensed with water in front.", category = EXPERIMENTAL)
    public static boolean dispenserWaterBottle;

    @Rule(desc = "Minecarts can be filled with hoppers, chests, tnt and furnace.", category = EXPERIMENTAL)
    public static boolean dispenserMinecartFiller;

    @Rule(desc = "Customizable tile tick limit", category = SURVIVAL, options = {"1000", "65536", "1000000"}, validator = "validateTileTickLimit", extra = {
            "-1 for no limit"
    })
    public static int tileTickLimit = 65536;
    private static boolean validateTileTickLimit(int value) {
        return value >= -1;
    }

    @Rule(desc = "Redstone ore blocks can redirect redstone dust", category = {EXPERIMENTAL, FEATURE})
    public static boolean redstoneOreRedirectsDust = false;

    @Rule(desc = "Adds back the crafting window duplication bug.", category = EXPERIMENTAL)
    public static boolean craftingWindowDuplication = false;

    @Rule(desc = "Adds back farmland bug where entities teleport on top of farmland that turns back to dirt.", category = EXPERIMENTAL)
    public static boolean farmlandBug;

    @Rule(desc = "Allows bedrock to drop as bedrock item if broken, similar to 1.8 and lower versions.", category = EXPERIMENTAL)
    public static boolean bedrockDropsAsItem;

    // ===== API ===== //

    /**
     * Any field in this class annotated with this class is interpreted as a carpet rule.
     * The field must be static and have a type of one of:
     * - boolean
     * - int
     * - double
     * - String
     * - a subclass of Enum
     * The default value of the rule will be the initial value of the field.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Rule {
        /**
         * The rule name, by default the same as the field name
         */
        String name() default ""; // default same as field name

        /**
         * A description of the rule
         */
        String desc();

        /**
         * Extra information about the rule
         */
        String[] extra() default {};

        /**
         * A list of categories the rule is in
         */
        RuleCategory[] category();

        /**
         * Options to select in menu and in carpet client.
         * Inferred for booleans and enums. Otherwise, must be present.
         */
        String[] options() default {};

        /**
         * The name of the validator method called when the rule is changed.
         * The validator method must:
         * - be declared in CarpetSettings
         * - be static
         * - have a return type of boolean
         * - have a single parameter whose type is the same as that of the rule
         * The validator returns true if the value of the rule is accepted, and false otherwise.
         */
        String validator() default ""; // default no method
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface CreativeDefault {
        String value() default "true";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface SurvivalDefault {
        String value() default "true";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface BugFixDefault {
        String value() default "true";
    }

    public static enum RuleCategory {
        TNT, FIX, SURVIVAL, CREATIVE, EXPERIMENTAL, OPTIMIZATIONS, FEATURE, COMMANDS
    }

    private static boolean validatePositive(int value) {
        return value > 0;
    }

    private static boolean validateNonNegative(int value) {
        return value >= 0;
    }


    // ===== IMPLEMENTATION ===== //

    private static Map<String, Field> rules = new HashMap<>();
    private static Map<String, String> defaults = new HashMap<>();
    static {
        for (Field field : CarpetSettings.class.getFields()) {
            if (field.isAnnotationPresent(Rule.class)) {
                Rule rule = field.getAnnotation(Rule.class);
                String name = rule.name().isEmpty() ? field.getName() : rule.name();

                if (field.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC))
                    throw new AssertionError("Access modifiers of rule field for \"" + name + "\" should be \"public static\"");

                if (field.getType() != boolean.class && field.getType() != int.class && field.getType() != double.class
                        && field.getType() != String.class && !field.getType().isEnum()) {
                    throw new AssertionError("Rule \"" + name + "\" has invalid type");
                }

                Object def;
                try {
                    def = field.get(null);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                if (def == null)
                    throw new AssertionError("Rule \"" + name + "\" has null default value");

                if (field.getType() != boolean.class && !field.getType().isEnum()) {
                    boolean containsDefault = false;
                    for (String option : rule.options()) {
                        Object val;
                        if (field.getType() == int.class) {
                            try {
                                val = Integer.parseInt(option);
                            } catch (NumberFormatException e) {
                                throw new AssertionError("Rule \"" + name + "\" has invalid option \"" + option + "\"");
                            }
                        } else if (field.getType() == double.class) {
                            try {
                                val = Double.parseDouble(option);
                            } catch (NumberFormatException e) {
                                throw new AssertionError("Rule \"" + name + "\" has invalid option \"" + option + "\"");
                            }
                        } else {
                            val = option;
                        }
                        if (val.equals(def))
                            containsDefault = true;
                    }
                    if (!containsDefault) {
                        throw new AssertionError("Default value of \"" + def + "\" for rule \"" + name + "\" is not included in its options. This is required for Carpet Client to work.");
                    }
                }

                String validator = rule.validator();
                if (!validator.isEmpty()) {
                    Method method;
                    try {
                        method = CarpetSettings.class.getDeclaredMethod(validator, field.getType());
                    } catch (NoSuchMethodException e) {
                        throw new AssertionError("Validator \"" + validator + "\" for rule \"" + name + "\" doesn't exist");
                    }
                    if (!Modifier.isStatic(method.getModifiers()) || method.getReturnType() != boolean.class) {
                        throw new AssertionError("Validator \"" + validator + "\" for rule \"" + name + "\" must be a static method returning a boolean");
                    }
                }

                rules.put(name.toLowerCase(Locale.ENGLISH), field);
                defaults.put(name.toLowerCase(Locale.ENGLISH), String.valueOf(def));
            }
        }
    }

    public static boolean hasRule(String ruleName) {
        return rules.containsKey(ruleName.toLowerCase(Locale.ENGLISH));
    }

    public static String get(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return "false";
        try {
            return String.valueOf(field.get(null));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static String getDescription(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return "Error";
        return field.getAnnotation(Rule.class).desc();
    }

    public static RuleCategory[] getCategories(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return new RuleCategory[0];
        return field.getAnnotation(Rule.class).category();
    }

    public static String getDefault(String ruleName) {
        String def = defaults.get(ruleName.toLowerCase(Locale.ENGLISH));
        return def == null ? "false" : locked && ruleName.startsWith("command") ? "false" : def;
    }

    @SuppressWarnings("unchecked")
    public static String[] getOptions(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null || field.getType() == boolean.class) {
            return new String[]{"false", "true"};
        } else if (field.getType().isEnum()) {
            return Arrays.stream(((Class<? extends Enum<?>>) field.getType()).getEnumConstants())
                    .map(Enum::name).toArray(String[]::new);
        } else {
            return field.getAnnotation(Rule.class).options();
        }
    }

    public static String[] getExtraInfo(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return new String[0];
        return field.getAnnotation(Rule.class).extra();
    }

    public static String getActualName(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return "null";
        String name = field.getAnnotation(Rule.class).name();
        return name.isEmpty() ? field.getName() : name;
    }

    public static boolean isDouble(String ruleName) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return false;
        else
            return field.getType() == double.class;
    }

    @SuppressWarnings("unchecked")
    public static boolean set(String ruleName, String value) {
        Field field = rules.get(ruleName.toLowerCase(Locale.ENGLISH));
        if (field == null)
            return false;

        Class<?> fieldType = field.getType();
        Object newValue;
        if (fieldType == boolean.class) {
            if ("true".equalsIgnoreCase(value))
                newValue = true;
            else if ("false".equalsIgnoreCase(value))
                newValue = false;
            else
                return false;
        } else if (fieldType == int.class) {
            try {
                newValue = new Integer(value);
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (fieldType == double.class) {
            try {
                newValue = new Double(value);
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (fieldType == String.class) {
            newValue = value;
        } else if (fieldType.isEnum()) {
            newValue = null;
            for (Enum<?> constant : ((Class<? extends Enum<?>>) fieldType).getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(value)) {
                    newValue = constant;
                    break;
                }
            }
            if (newValue == null)
                return false;
        } else {
            throw new AssertionError("Rule \"" + ruleName + "\" has an invalid type");
        }

        String validatorMethod = field.getDeclaredAnnotation(Rule.class).validator();
        if (!validatorMethod.isEmpty()) {
            try {
                Method validator = CarpetSettings.class.getDeclaredMethod(validatorMethod, field.getType());
                if (!((Boolean) validator.invoke(null, newValue)))
                    return false;
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        try {
            field.set(null, newValue);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }

        CarpetClientRuleChanger.updateCarpetClientsRule(ruleName, value);

        return true;
    }

    public static String[] findNonDefault() {
        List<String> rules = new ArrayList<>();
        for (String rule : CarpetSettings.rules.keySet())
            if (!get(rule).equalsIgnoreCase(getDefault(rule)))
                rules.add(getActualName(rule));
        Collections.sort(rules);
        return rules.toArray(new String[0]);
    }

    public static String[] findAll(String filter) {
        String actualFilter = filter == null ? null : filter.toLowerCase(Locale.ENGLISH);
        return rules.keySet().stream()
                .filter(rule -> {
                    if (actualFilter == null) return true;
                    if (rule.contains(actualFilter)) return true;
                    for (RuleCategory ctgy : getCategories(rule))
                        if (ctgy.name().equalsIgnoreCase(actualFilter))
                            return true;
                    return false;
                })
                .map(CarpetSettings::getActualName)
                .sorted()
                .toArray(String[]::new);
    }

    public static void resetToUserDefaults(MinecraftServer server)
    {
        resetToVanilla();
        applySettingsFromConf(server);
    }

    public static void resetToVanilla() {
        for (String rule : rules.keySet()) {
            set(rule, getDefault(rule));
        }
    }

    public static void resetToBugFixes() {
        resetToVanilla();
        rules.forEach((name, field) -> {
            if (field.isAnnotationPresent(BugFixDefault.class)) {
                set(name, field.getAnnotation(BugFixDefault.class).value());
            }
        });
    }

    public static void resetToCreative() {
        resetToBugFixes();
        rules.forEach((name, field) -> {
            if (field.isAnnotationPresent(CreativeDefault.class)) {
                set(name, field.getAnnotation(CreativeDefault.class).value());
            }
        });
    }

    public static void resetToSurvival() {
        resetToBugFixes();
        rules.forEach((name, field) -> {
            if (field.isAnnotationPresent(SurvivalDefault.class)) {
                set(name, field.getAnnotation(SurvivalDefault.class).value());
            }
        });
    }


    // ===== CONFIG ===== //

    public static void applySettingsFromConf(MinecraftServer server)
    {
        Map<String, String> conf = readConf(server);
        boolean is_locked = locked;
        locked = false;
        if (is_locked)
        {
            LOG.info("[CM]: Carpet Mod is locked by the administrator");
        }
        for (String key: conf.keySet())
        {
            if (!set(key, conf.get(key)))
                LOG.error("[CM]: The value of " + conf.get(key) + " for " + key + " is not valid - ignoring...");
            else
                LOG.info("[CM]: loaded setting "+key+" as "+conf.get(key)+" from carpet.conf");
        }
        locked = is_locked;
    }

    private static Map<String, String> readConf(MinecraftServer server)
    {
        try
        {
            File settings_file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "carpet.conf");
            BufferedReader b = new BufferedReader(new FileReader(settings_file));
            String line = "";
            Map<String,String> result = new HashMap<String, String>();
            while ((line = b.readLine()) != null)
            {
                line = line.replaceAll("\\r|\\n", "");
                if ("locked".equalsIgnoreCase(line))
                {
                    locked = true;
                }
                String[] fields = line.split("\\s+",2);
                if (fields.length > 1)
                {
                    if (!hasRule(fields[0]))
                    {
                        LOG.error("[CM]: Setting " + fields[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    result.put(fields[0],fields[1]);
                }
            }
            b.close();
            return result;
        }
        catch(FileNotFoundException e)
        {
            return new HashMap<String, String>();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new HashMap<String, String>();
        }

    }
    private static void writeConf(MinecraftServer server, Map<String, String> values)
    {
        if (locked) return;
        try
        {
            File settings_file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "carpet.conf");
            FileWriter fw = new FileWriter(settings_file);
            for (String key: values.keySet())
            {
                fw.write(key+" "+values.get(key)+"\n");
            }
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            LOG.error("[CM]: failed write the carpet.conf");
        }
    }

    // stores different defaults in the file
    public static boolean addOrSetPermarule(MinecraftServer server, String setting_name, String string_value)
    {
        if (locked) return false;
        if (hasRule(setting_name))
        {
            Map<String, String> conf = readConf(server);
            conf.put(setting_name, string_value);
            writeConf(server, conf);
            return set(setting_name,string_value);
        }
        return false;
    }
    // removes overrides of the default values in the file
    public static boolean removePermarule(MinecraftServer server, String setting_name)
    {
        if (locked) return false;
        if (hasRule(setting_name))
        {
            Map<String, String> conf = readConf(server);
            conf.remove(setting_name);
            writeConf(server, conf);
            return set(setting_name,getDefault(setting_name));
        }
        return false;
    }

    public static String[] findStartupOverrides(MinecraftServer server)
    {
        ArrayList<String> res = new ArrayList<String>();
        if (locked) return res.toArray(new String[0]);
        Map <String,String> defaults = readConf(server);
        for (String rule: rules.keySet().stream().sorted().collect(Collectors.toList()))
        {
            if (defaults.containsKey(rule))
            {
                res.add(get(rule));
            }
        }
        return res.toArray(new String[0]);
    }
}
