package carpet.utils.extensions;

public interface ExtendedEndChunkGenerator {
    void setEndChunkSeed(long seed);
    long getLastRandomSeed();
    void setRandomSeedUsed(boolean used);
    boolean wasRandomSeedUsed();
}
