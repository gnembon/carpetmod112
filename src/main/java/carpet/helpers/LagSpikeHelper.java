package carpet.helpers;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LagSpikeHelper {

    private static @Nullable DimensionType pendingLagDimension;
    private static TickPhase pendingLagPhase;
    private static Enum<?> pendingLagSubPhase;
    private static long pendingLagTime;

    public static void addLagSpike(@Nullable DimensionType dimension, TickPhase phase, Enum<?> subPhase, long millis) {
        pendingLagDimension = dimension;
        pendingLagPhase = phase;
        pendingLagSubPhase = subPhase;
        pendingLagTime = millis;
    }

    /**
     * Safe to call without carpet rule as {@link #pendingLagPhase} is always {@code null} in vanilla
     */
    public static void processLagSpikes(@Nullable World world, TickPhase phase, Enum<?> subPhase) {
        if (phase == pendingLagPhase && subPhase == pendingLagSubPhase && (world == null || world.provider.getDimensionType() == pendingLagDimension)) {
            try {
                Thread.sleep(pendingLagTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pendingLagPhase = null;
            pendingLagSubPhase = null;
            pendingLagDimension = null;
        }
    }

    public enum TickPhase {
        PLAYER(PrePostSubPhase.class, PrePostSubPhase.PRE),
        MOB_SPAWNING(PrePostSubPhase.class, PrePostSubPhase.PRE),
        CHUNK_UNLOADING(PrePostSubPhase.class, PrePostSubPhase.PRE),
        TILE_TICK(PrePostSubPhase.class, PrePostSubPhase.PRE),
        RANDOM_TICK(PrePostSubPhase.class, PrePostSubPhase.PRE),
        PLAYER_CHUNK_MAP(PrePostSubPhase.class, PrePostSubPhase.PRE),
        VILLAGE(PrePostSubPhase.class, PrePostSubPhase.PRE),
        BLOCK_EVENT(PrePostSubPhase.class, PrePostSubPhase.PRE),
        ENTITY(EntitySubPhase.class, EntitySubPhase.PRE),
        TILE_ENTITY(PrePostSubPhase.class, PrePostSubPhase.PRE),
        AUTOSAVE(PrePostSubPhase.class, PrePostSubPhase.PRE),

        TICK(PrePostSubPhase.class, PrePostSubPhase.PRE),
        DIMENSION(PrePostSubPhase.class, PrePostSubPhase.PRE),
        ;

        private final Class<? extends Enum<?>> subPhaseClass;
        private final Enum<?> defaultSubPhase;
        <T extends Enum<T>> TickPhase(Class<T> subPhaseClass, T defaultSubPhase) {
            this.subPhaseClass = subPhaseClass;
            this.defaultSubPhase = defaultSubPhase;
        }

        public boolean isDimensionApplicable() {
            return this != PLAYER && this != AUTOSAVE && this != TICK;
        }

        public Class<? extends Enum<?>> getSubPhaseClass() {
            return subPhaseClass;
        }

        public Enum<?> getDefaultSubPhase() {
            return defaultSubPhase;
        }
    }

    public enum PrePostSubPhase {
        PRE, POST
    }

    public enum EntitySubPhase {
        PRE, POST_WEATHER, POST_NORMAL, POST_PLAYERS
    }
}
