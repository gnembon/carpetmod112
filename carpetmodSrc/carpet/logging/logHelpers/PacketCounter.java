package carpet.logging.logHelpers;

public class PacketCounter
{
    public static long totalOut;
    public static long totalIn;
    public static void reset() {totalIn = 0L; totalOut = 0L; }
}
