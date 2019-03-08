package carpet.carpetclient;

import java.util.ArrayList;
import java.util.List;

import carpet.CarpetSettings;
import com.google.common.base.Charsets;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;

public class CarpetClientServer {

    private MinecraftServer minecraftServer;
    private static ArrayList<EntityPlayerMP> players = new ArrayList<>();
    public static final String CARPET_CHANNEL_NAME = "CarpetClient";

    public CarpetClientServer(MinecraftServer server) {
        this.minecraftServer = server;
    }

    public void onCustomPayload(EntityPlayerMP sender, String channel, PacketBuffer data) {
        if (CARPET_CHANNEL_NAME.equals(channel)) {
            CarpetClientMessageHandler.handler(sender, data);
        }
    }

    public void onChannelRegister(EntityPlayerMP sender, List<String> channels) {
        if (channels.contains(CARPET_CHANNEL_NAME)) {
            players.add(sender);
            CarpetClientMessageHandler.sendAllGUIOptions();
        }
    }

    public void onChannelUnregister(EntityPlayerMP sender, List<String> channels) {
    }

    public void onPlayerConnect(EntityPlayerMP player) {
        player.connection.sendPacket(new SPacketCustomPayload("REGISTER",
                new PacketBuffer(Unpooled.wrappedBuffer(CARPET_CHANNEL_NAME.getBytes(Charsets.UTF_8)))));
    }

    public void onPlayerDisconnect(EntityPlayerMP player) {
        players.remove(player);
        CarpetClientMarkers.unregisterPlayerVillageMarkers(player);
        CarpetClientChunkLogger.logger.unregisterPlayer(player);
        CarpetClientRandomtickingIndexing.unregisterPlayer(player);
    }

    static public ArrayList<EntityPlayerMP> getRegisteredPlayers() {
        return players;
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
