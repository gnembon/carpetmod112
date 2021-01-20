package narcolepticfrog.rsmm.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerConnectionEventDispatcher {

    private PlayerConnectionEventDispatcher() {}

    private static List<PlayerConnectionListener> listeners = new ArrayList<>();

    public static void addListener(PlayerConnectionListener listener) {
        listeners.add(listener);
    }

    public static void dispatchPlayerConnectEvent(ServerPlayerEntity player) {
        for (PlayerConnectionListener listener : listeners) {
            listener.onPlayerConnect(player);
        }
    }

    public static void dispatchPlayerDisconnectEvent(ServerPlayerEntity player) {
        for (PlayerConnectionListener listener : listeners) {
            listener.onPlayerDisconnect(player);
        }
    }

}
