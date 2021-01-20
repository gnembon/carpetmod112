package carpet.helpers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ItemEntity;

/**
 * Class to work around the fact that blocks create EntityItems without returning them
 */
public class CapturedDrops
{
    private static boolean isCapturingDrops = false;
    private static List<ItemEntity> lastCapturedDrops = new ArrayList<>();
    
    public static void setCapturingDrops(boolean capturingDrops)
    {
        isCapturingDrops = capturingDrops;
    }
    
    public static boolean isCapturingDrops()
    {
        return isCapturingDrops;
    }
    
    public static void clearCapturedDrops()
    {
        lastCapturedDrops.clear();
    }
    
    public static List<ItemEntity> getCapturedDrops()
    {
        return lastCapturedDrops;
    }
    
    public static void captureDrop(ItemEntity item)
    {
        lastCapturedDrops.add(item);
    }
}
