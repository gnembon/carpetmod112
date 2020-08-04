package carpet.helpers;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.mixin.accessors.BlockAccessor;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayList;
import java.util.List;

public class RandomTickOptimization {

    private static List<Block> USELESS_RANDOMTICKS = new ArrayList<>();
    public static boolean needsWorldGenFix = false;

    static {
        for (Block b : Block.REGISTRY) {
            if (b instanceof BlockBasePressurePlate
                || b instanceof BlockButton
                || b instanceof BlockPumpkin
                || b instanceof BlockRedstoneTorch) {
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
        USELESS_RANDOMTICKS.forEach(b -> ((BlockAccessor) b).invokeSetTickRandomly(on));
    }

    public static void setLiquidRandomTicks(boolean on) {
        needsWorldGenFix = !on;
        ((BlockAccessor) Blocks.FLOWING_WATER).invokeSetTickRandomly(on);
        ((BlockAccessor) Blocks.FLOWING_LAVA).invokeSetTickRandomly(on);
    }

    public static void setSpongeRandomTicks(boolean on) {
        ((BlockAccessor) Blocks.SPONGE).invokeSetTickRandomly(on);
    }

    public static void recalculateAllChunks() {
        if (CarpetServer.minecraft_server.worlds == null) // worlds not loaded yet
            return;
        for (World world : CarpetServer.minecraft_server.worlds) {
            IChunkProvider provider = world.getChunkProvider();
            if (!(provider instanceof ChunkProviderServer))
                continue;
            for (Chunk chunk : ((ChunkProviderServer) provider).loadedChunks.values()) {
                for (ExtendedBlockStorage subchunk : chunk.getBlockStorageArray()) {
                    if (subchunk != null)
                        subchunk.recalculateRefCounts();
                }
            }
        }
    }

}
