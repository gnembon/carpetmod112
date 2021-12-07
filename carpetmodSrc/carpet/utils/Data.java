package carpet.utils;

import carpet.CarpetSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Data {
    //setup: -26, 9
    //test2: 1071, 1000
    //setup: -26, 9
    //test2: 1071, 1000
    public static final int rehashChunkX = 1071, rehashChunkZ = 1000;

    public static List<ArrayList<Long>> times = new ArrayList<>();

    public static List<Long> glassArrivalTimes = new ArrayList<>();
    public static long reloadChunkFromQueueStartTime = 0;
    public static long dataInputStreamStart = 0;
    public static long dataFixerStart = 0;
    public static long checkedReadStart = 0;
    public static long recreateStructuresStart = 0;
    public static long chunkLoadEnd = 0;
    public static long chunkInsertStart = 0;

    public static long removeStart = 0;
    public static long removeEnd = 0;
    public static ThreadLocal<Boolean> mainThread = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<ArrayList<Long>> threadArray = ThreadLocal.withInitial(() -> new ArrayList<>());
    public static long chunkHash = 575525617733L; // the chunk value
    public static long chunkGet = -360777252790L;
}