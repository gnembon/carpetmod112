package carpet.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PluginChannelManager {
    public static final Logger LOG = LogManager.getLogger();

    private final MinecraftServer server;
    public final PluginChannelTracker tracker;
    private Map<String, PluginChannelHandler> channelHandlers = new HashMap<>();

    public PluginChannelManager(MinecraftServer server) {
        this.server = server;
        this.tracker = new PluginChannelTracker(server);
    }

    public void register(PluginChannelHandler handler) {
        String[] channels = handler.getChannels();
        for (String channel : channels) {
            channelHandlers.put(channel, handler);
        }
        PlayerManager playerList = server.getPlayerManager();
        // make sure server started up
        if (playerList != null) {
            sendChannelUpdate(playerList.getPlayerList(), "REGISTER", Arrays.asList(channels));
        }
    }

    public void unregister(PluginChannelHandler handler) {
        String[] channels = handler.getChannels();
        for (String channel : channels) {
            for (ServerPlayerEntity player : tracker.getPlayers(channel)) {
                handler.unregister(channel, player);
                tracker.unregister(player, channel);
            }
            channelHandlers.remove(channel);
        }
        sendChannelUpdate(server.getPlayerManager().getPlayerList(), "UNREGISTER", Arrays.asList(channels));
    }

    public void process(ServerPlayerEntity player, CustomPayloadC2SPacket packet) {
        String channel = packet.method_32939();
        PacketByteBuf payload = packet.method_32941();
        switch(channel) {
            case "REGISTER": {
                this.processRegister(player, payload);
                return;
            }
            case "UNREGISTER": {
                this.processUnregister(player, payload);
                return;
            }
        }
        PluginChannelHandler handler = channelHandlers.get(channel);
        if (handler != null) {
            handler.onCustomPayload(packet, player);
        }
    }

    private void processRegister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<String> channels = getChannels(payload);
        for (String channel : channels) {
            PluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null && handler.register(channel, player)) {
                tracker.register(player, channel);
            }
        }
    }

    private void processUnregister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<String> channels = getChannels(payload);
        for (String channel : channels) {
            if (!tracker.isRegistered(player, channel)) continue;
            PluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null) handler.unregister(channel, player);
            tracker.unregister(player, channel);
        }
    }

    public void onPlayerConnected(ServerPlayerEntity player) {
        sendChannelUpdate(Collections.singleton(player), "REGISTER", channelHandlers.keySet());
    }

    public void onPlayerDisconnected(ServerPlayerEntity player) {
        for (Map.Entry<String, PluginChannelHandler> handler : channelHandlers.entrySet()) {
            handler.getValue().unregister(handler.getKey(), player);
        }
        tracker.unregisterAll(player);
    }

    private void sendChannelUpdate(Collection<ServerPlayerEntity> players, String updateType, Collection<String> channels) {
        if (players.isEmpty()) return;
        String joinedChannels = String.join("\0", channels);
        ByteBuf payload = Unpooled.wrappedBuffer(joinedChannels.getBytes(Charsets.UTF_8));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(updateType, new PacketByteBuf(payload));
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(packet);
        }
    }

    private static List<String> getChannels(PacketByteBuf buff) {
        buff.resetReaderIndex();
        byte[] bytes = new byte[buff.readableBytes()];
        buff.readBytes(bytes);
        String channelString = new String(bytes, Charsets.UTF_8);
        return Lists.newArrayList(channelString.split("\u0000"));
    }
}
