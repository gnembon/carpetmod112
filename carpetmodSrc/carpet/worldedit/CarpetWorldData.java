package carpet.worldedit;

import com.sk89q.worldedit.world.registry.BiomeRegistry;
import com.sk89q.worldedit.world.registry.LegacyWorldData;

/**
 * World data for the Carpet platform.
 */
class CarpetWorldData extends LegacyWorldData {

    private static final CarpetWorldData INSTANCE = new CarpetWorldData();
    private final BiomeRegistry biomeRegistry = new CarpetBiomeRegistry();

    /**
     * Create a new instance.
     */
    CarpetWorldData() {
    }

    @Override
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }

    /**
     * Get a static instance.
     *
     * @return an instance
     */
    public static CarpetWorldData getInstance() {
        return INSTANCE;
    }

}
