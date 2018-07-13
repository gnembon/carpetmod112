package narcolepticfrog.rsmm.events;

import java.util.ArrayList;
import java.util.List;

public class TickStartEventDispatcher {

    private TickStartEventDispatcher() {}

    private static List<TickStartListener> listeners = new ArrayList<>();

    public static void addListener(TickStartListener listener) {
        listeners.add(listener);
    }

    public static void dispatchEvent(int tick) {
        for (TickStartListener listener : listeners) {
            listener.onTickStart(tick);
        }
    }

}
