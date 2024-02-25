package carpet.utils;

import java.util.Random;

public class RandomTools {
    public static double nextNotGaussian(Random rand) {
        rand.nextDouble();
        return 16.0D * rand.nextDouble() - 8.0D;
    }
}
