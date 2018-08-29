package carpet.worldedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import net.minecraft.world.biome.Biome;

/**
 * Provides access to biome data in Minecraft.
 */
class CarpetBiomeRegistry implements BiomeRegistry {
    private static Map<Integer, Biome> biomes = Collections.emptyMap();
    private static Map<Integer, BiomeData> biomeData = Collections.emptyMap();

    @Nullable
    @Override
    public BaseBiome createFromId(int id) {
        return new BaseBiome(id);
    }

    @Override
    public List<BaseBiome> getBiomes() {
        List<BaseBiome> list = new ArrayList<BaseBiome>();
        for (int biome : biomes.keySet()) {
            list.add(new BaseBiome(biome));
        }
        return list;
    }

    @Nullable
    @Override
    public BiomeData getData(BaseBiome biome) {
        return biomeData.get(biome.getId());
    }

    /**
     * Populate the internal static list of biomes.
     *
     * <p>If called repeatedly, the last call will overwrite all previous
     * calls.</p>
     */
    static void populate() {
        Map<Integer, Biome> biomes = HashBiMap.create();
        Map<Integer, BiomeData> biomeData = new HashMap<Integer, BiomeData>();

        for (Biome biome : Biome.REGISTRY) {
            if ((biome == null) || (biomes.containsValue(biome))) {
                continue;
            }
            biomes.put(Biome.getIdForBiome(biome), biome);
            biomeData.put(Biome.getIdForBiome(biome), new CarpetBiomeData(biome));
        }

        CarpetBiomeRegistry.biomes = biomes;
        CarpetBiomeRegistry.biomeData = biomeData;
    }

    /**
     * Cached biome data information.
     */
    private static class CarpetBiomeData implements BiomeData {
        private final Biome biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private CarpetBiomeData(Biome biome) {
            this.biome = biome;
        }

        @Override
        public String getName() {
            return biome.biomeName;
        }
    }

}