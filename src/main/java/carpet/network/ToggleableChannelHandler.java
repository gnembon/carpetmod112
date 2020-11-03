package carpet.network;

import carpet.CarpetServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketCustomPayload;

public class ToggleableChannelHandler implements PluginChannelHandler {
    private final PluginChannelManager channelManager;
    public final PluginChannelHandler baseHandler;
    private boolean enabled;

    public ToggleableChannelHandler(PluginChannelManager channelManager, PluginChannelHandler baseHandler) {
        this(channelManager, baseHandler, true);
    }

    public ToggleableChannelHandler(PluginChannelManager channelManager, PluginChannelHandler baseHandler, boolean enabled) {
        this.channelManager = channelManager;
        this.baseHandler = baseHandler;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            if (enabled) {
                channelManager.register(this);
            } else {
                channelManager.unregister(this);
            }
            this.enabled = enabled;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String[] getChannels() {
        return baseHandler.getChannels();
    }

    @Override
    public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
        if (enabled) baseHandler.onCustomPayload(packet, player);
    }

    @Override
    public boolean register(String channel, EntityPlayerMP player) {
        return enabled && baseHandler.register(channel, player);
    }

    @Override
    public void unregister(String channel, EntityPlayerMP player) {
        baseHandler.unregister(channel, player);
    }
}
