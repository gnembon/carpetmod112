package carpet.network;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PluginChannelHandler {
    String[] getChannels();

    default boolean register(String channel, ServerPlayerEntity player) {
        return true;
    }

    default void unregister(String channel, ServerPlayerEntity player) {

    }

    void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player);
}
