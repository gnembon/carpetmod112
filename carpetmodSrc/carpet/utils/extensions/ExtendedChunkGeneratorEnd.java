package carpet.utils.extensions;

public interface ExtendedChunkGeneratorEnd {
    void setEndChunkSeed(long seed);
    long getLastRandomSeed();
    void setRandomSeedUsed(boolean used);
    boolean wasRandomSeedUsed();
}
