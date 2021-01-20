package narcolepticfrog.rsmm.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PistonPushEventDispatcher {

    private PistonPushEventDispatcher() {}

    private static List<PistonPushListener> listeners = new ArrayList<>();

    public static void addListener(PistonPushListener l) {
        listeners.add(l);
    }

    public static void dispatchEvent(World w, BlockPos p, Direction dir) {
        for (PistonPushListener listener : listeners) {
            listener.onPistonPush(w, p, dir);
        }
    }

}
