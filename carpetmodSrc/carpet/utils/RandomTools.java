package carpet.utils;

import java.util.Random;

public class RandomTools {
    static boolean isOddCall = false;
    static final double MIN_ODD = -7.844680087923773D;
    static final double MIN_EVEN = -7.799011049744149D;
    static final double MAX_ODD = 7.973782613935931D;
    static final double MAX_EVEN = 7.995084298635286D;

    public static double nextNotGaussian(Random rand) {
        double result;
        //Biggest number lower than 1 Binary: 0011111111101111111111111111111111111111111111111111111111111111 which 0.9999999999999999 rounds up to
        double nextDouble = rand.nextDouble();

        if (isOddCall) {
            if (nextDouble == 0.9999999999999999D) {
                result = MAX_ODD;
            }
            else {
                result = MIN_ODD + (MAX_ODD - MIN_ODD) * nextDouble;
            }
        }
        else {
            if (nextDouble == 0.9999999999999999D) {
                result = MAX_EVEN;
            }
            else {
                result = MIN_EVEN + (MAX_EVEN - MIN_EVEN) * nextDouble;
            }
        }
        isOddCall = !isOddCall;

        return result;
    }
}
