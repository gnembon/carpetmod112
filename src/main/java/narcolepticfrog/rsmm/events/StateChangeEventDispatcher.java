package narcolepticfrog.rsmm.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StateChangeEventDispatcher {

    private StateChangeEventDispatcher() {}

    private static List<StateChangeListener> listeners = new ArrayList<>();

    public static void addListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    public static void dispatchEvent(World world, BlockPos pos) {
        for (StateChangeListener listener : listeners) {
            listener.onStateChange(world, pos);
        }
    }

}
