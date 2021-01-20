package narcolepticfrog.rsmm.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class ServerPacketEventDispatcher {

    private ServerPacketEventDispatcher() {}

    private static List<ServerPacketListener> listeners = new ArrayList<>();

    public static void addListener(ServerPacketListener listener) {
        listeners.add(listener);
    }

    public static void dispatchCustomPayload(ServerPlayerEntity sender, String channel, PacketByteBuf data) {
        for (ServerPacketListener listener : listeners) {
            listener.onCustomPayload(sender, channel, data);
        }
    }

    public static void dispatchChannelRegister(ServerPlayerEntity sender, List<String> channels) {
        for (ServerPacketListener listener : listeners) {
            listener.onChannelRegister(sender, channels);
        }
    }

    public static void dispatchChannelUnregister(ServerPlayerEntity sender, List<String> channels) {
        for (ServerPacketListener listener : listeners) {
            listener.onChannelUnregister(sender, channels);
        }
    }
}
