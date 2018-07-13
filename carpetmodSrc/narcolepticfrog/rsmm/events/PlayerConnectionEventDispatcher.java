package narcolepticfrog.rsmm.events;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

public class PlayerConnectionEventDispatcher {

    private PlayerConnectionEventDispatcher() {}

    private static List<PlayerConnectionListener> listeners = new ArrayList<>();

    public static void addListener(PlayerConnectionListener listener) {
        listeners.add(listener);
    }

    public static void dispatchPlayerConnectEvent(EntityPlayerMP player) {
        for (PlayerConnectionListener listener : listeners) {
            listener.onPlayerConnect(player);
        }
    }

    public static void dispatchPlayerDisconnectEvent(EntityPlayerMP player) {
        for (PlayerConnectionListener listener : listeners) {
            listener.onPlayerDisconnect(player);
        }
    }

}
