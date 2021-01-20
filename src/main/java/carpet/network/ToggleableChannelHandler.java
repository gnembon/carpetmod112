package carpet.network;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

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
    public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
        if (enabled) baseHandler.onCustomPayload(packet, player);
    }

    @Override
    public boolean register(String channel, ServerPlayerEntity player) {
        return enabled && baseHandler.register(channel, player);
    }

    @Override
    public void unregister(String channel, ServerPlayerEntity player) {
        baseHandler.unregister(channel, player);
    }
}
