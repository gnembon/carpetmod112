package carpet.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketCustomPayload;

public interface PluginChannelHandler {
    String[] getChannels();

    default boolean register(String channel, EntityPlayerMP player) {
        return true;
    }

    default void unregister(String channel, EntityPlayerMP player) {

    }

    void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player);
}
