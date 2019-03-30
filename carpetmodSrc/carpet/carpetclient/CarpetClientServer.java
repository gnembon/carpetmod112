package carpet.carpetclient;

import java.util.ArrayList;
import java.util.List;

import carpet.CarpetSettings;
import carpet.network.PluginChannelHandler;
import com.google.common.base.Charsets;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;

public class CarpetClientServer implements PluginChannelHandler {

    private MinecraftServer minecraftServer;
    private static ArrayList<EntityPlayerMP> players = new ArrayList<>();
    public static final String CARPET_CHANNEL_NAME = "CarpetClient";

    public CarpetClientServer(MinecraftServer server) {
        this.minecraftServer = server;
    }

    public String[] getChannels() {
        return new String[]{CARPET_CHANNEL_NAME};
    }

    public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
        CarpetClientMessageHandler.handler(player, packet.getBufferData());
    }

    public boolean register(String channel, EntityPlayerMP sender) {
        players.add(sender);
        CarpetClientMessageHandler.sendAllGUIOptions();
        return true;
    }

    public void unregister(String channel, EntityPlayerMP player) {
        players.remove(player);
        CarpetClientMarkers.unregisterPlayerVillageMarkers(player);
        CarpetClientChunkLogger.logger.unregisterPlayer(player);
        CarpetClientRandomtickingIndexing.unregisterPlayer(player);
    }

    static public ArrayList<EntityPlayerMP> getRegisteredPlayers() {
        return players;
    }

    public static boolean isPlayerRegistered(EntityPlayerMP player) {
        return players.contains(player);
    }

    public static boolean sendProtected(PacketBuffer data)
    {
        try
        {
            sender(data);
            return true;
        }
        catch (IllegalArgumentException exc)
        {
            // Payload too large
            return false;
        }
    }

    public static boolean sendProtected(PacketBuffer data, EntityPlayerMP player)
    {
        try
        {
            sender(data, player);
            return true;
        }
        catch (IllegalArgumentException exc)
        {
            // Payload too large
            return false;
        }
    }

    public static void sender(PacketBuffer data)
    {
        SPacketCustomPayload packet = new SPacketCustomPayload(CARPET_CHANNEL_NAME, data);
        for (EntityPlayerMP player : CarpetClientServer.getRegisteredPlayers()) {
            player.connection.sendPacket(packet);
        }
    }

    public static void sender(PacketBuffer data, EntityPlayerMP player)
    {
        SPacketCustomPayload packet = new SPacketCustomPayload(CARPET_CHANNEL_NAME, data);
        player.connection.sendPacket(packet);
    }

    public MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }
}
