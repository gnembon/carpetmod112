package carpet.utils.extensions;

public interface ExtendedFloatingIslandsChunkGenerator {
    void setEndChunkSeed(long seed);
    long getLastRandomSeed();
    void setRandomSeedUsed(boolean used);
    boolean wasRandomSeedUsed();
}
