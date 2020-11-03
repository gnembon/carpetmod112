package narcolepticfrog.rsmm.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;

public class ServerPacketEventDispatcher {

    private ServerPacketEventDispatcher() {}

    private static List<ServerPacketListener> listeners = new ArrayList<>();

    public static void addListener(ServerPacketListener listener) {
        listeners.add(listener);
    }

    public static void dispatchCustomPayload(EntityPlayerMP sender, String channel, PacketBuffer data) {
        for (ServerPacketListener listener : listeners) {
            listener.onCustomPayload(sender, channel, data);
        }
    }

    public static void dispatchChannelRegister(EntityPlayerMP sender, List<String> channels) {
        for (ServerPacketListener listener : listeners) {
            listener.onChannelRegister(sender, channels);
        }
    }

    public static void dispatchChannelUnregister(EntityPlayerMP sender, List<String> channels) {
        for (ServerPacketListener listener : listeners) {
            listener.onChannelUnregister(sender, channels);
        }
    }
}
