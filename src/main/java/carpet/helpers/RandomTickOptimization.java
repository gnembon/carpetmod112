package carpet.helpers;

import carpet.CarpetServer;
import carpet.mixin.accessors.BlockAccessor;
import carpet.mixin.accessors.ServerChunkCacheAccessor;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.chunk.ChunkSection;
import java.util.ArrayList;
import java.util.List;

public class RandomTickOptimization {

    private static List<Block> USELESS_RANDOMTICKS = new ArrayList<>();
    public static boolean needsWorldGenFix = false;

    static {
        for (Block b : Block.REGISTRY) {
            if (b instanceof AbstractPressurePlateBlock
                || b instanceof AbstractButtonBlock
                || b instanceof PumpkinBlock
                || b instanceof RedstoneTorchBlock) {
                USELESS_RANDOMTICKS.add(b);
            }
        }
        USELESS_RANDOMTICKS.add(Blocks.CAKE);
        USELESS_RANDOMTICKS.add(Blocks.CARPET);
        USELESS_RANDOMTICKS.add(Blocks.DETECTOR_RAIL);
        USELESS_RANDOMTICKS.add(Blocks.SNOW);
        USELESS_RANDOMTICKS.add(Blocks.TORCH);
        USELESS_RANDOMTICKS.add(Blocks.TRIPWIRE);
        USELESS_RANDOMTICKS.add(Blocks.TRIPWIRE_HOOK);
    }

    public static void setUselessRandomTicks(boolean on) {
        USELESS_RANDOMTICKS.forEach(b -> ((BlockAccessor) b).invokeSetRandomTicks(on));
    }

    public static void setLiquidRandomTicks(boolean on) {
        needsWorldGenFix = !on;
        ((BlockAccessor) Blocks.FLOWING_WATER).invokeSetRandomTicks(on);
        ((BlockAccessor) Blocks.FLOWING_LAVA).invokeSetRandomTicks(on);
    }

    public static void setSpongeRandomTicks(boolean on) {
        ((BlockAccessor) Blocks.SPONGE).invokeSetRandomTicks(on);
    }

    public static void recalculateAllChunks() {
        if (CarpetServer.minecraft_server.worlds == null) // worlds not loaded yet
            return;
        for (World world : CarpetServer.minecraft_server.worlds) {
            ChunkCache provider = world.getChunkManager();
            if (!(provider instanceof ServerChunkCache))
                continue;
            for (Chunk chunk : ((ServerChunkCacheAccessor) provider).getLoadedChunksMap().values()) {
                for (ChunkSection subchunk : chunk.getSections()) {
                    if (subchunk != null)
                        subchunk.method_27445();
                }
            }
        }
    }

}
