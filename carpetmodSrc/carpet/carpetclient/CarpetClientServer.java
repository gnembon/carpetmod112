package carpet.carpetclient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import carpet.CarpetSettings;
import carpet.network.PacketSplitter;
import carpet.network.PluginChannelHandler;
import com.google.common.base.Charsets;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CarpetClientServer implements PluginChannelHandler {

    private final MinecraftServer minecraftServer;
    private static final LinkedHashSet<EntityPlayerMP> players = new LinkedHashSet<>();
    public static final String CARPET_CHANNEL_NAME = "carpet:client";
    public static final String MINE_CHANNEL_NAME = "carpet:mine";

    public CarpetClientServer(MinecraftServer server) {
        this.minecraftServer = server;
    }

    public String[] getChannels() {
        return new String[]{CARPET_CHANNEL_NAME, MINE_CHANNEL_NAME};
    }

    public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
        switch (packet.getChannelName()) {
            case CARPET_CHANNEL_NAME:
                PacketBuffer buffer = PacketSplitter.receive(player, packet);
                if(buffer != null) {
                    CarpetClientMessageHandler.handler(player, buffer);
                }
                break;
            case MINE_CHANNEL_NAME:
                // Mining packets for carpet client to get around few bugs and careful break. CARPET-XCOM
                PacketBuffer payload = packet.getBufferData();
                payload.readBoolean();
                boolean start = payload.readBoolean();
                BlockPos pos = payload.readBlockPos();
                EnumFacing facing = EnumFacing.byIndex(payload.readUnsignedByte());
                PlayerInteractionManager.activateInstantMine = payload.readBoolean();
                if (start) {
                    if (!this.minecraftServer.isBlockProtected(player.world, pos, player) && player.world.getWorldBorder().contains(pos)) {
                        player.interactionManager.onBlockClicked(pos, facing);
                    } else {
                        player.connection.sendPacket(new SPacketBlockChange(player.world, pos));
                    }
                } else {
                    player.interactionManager.blockRemoving(pos);
                }
                PlayerInteractionManager.activateInstantMine = true;
                break;
        }
    }

    public boolean register(String channel, EntityPlayerMP sender) {
        players.add(sender);
        CarpetClientMessageHandler.sendAllGUIOptions(sender);
        CarpetClientMessageHandler.sendCustomRecipes(sender);
        return true;
    }

    public void unregister(String channel, EntityPlayerMP player) {
        players.remove(player);
        CarpetClientMarkers.unregisterPlayerVillageMarkers(player);
        CarpetClientChunkLogger.logger.unregisterPlayer(player);
        CarpetClientRandomtickingIndexing.unregisterPlayer(player);
    }

    static public LinkedHashSet<EntityPlayerMP> getRegisteredPlayers() {
        return players;
    }

    public static boolean isPlayerRegistered(EntityPlayerMP player) {
        return players.contains(player);
    }

    public static boolean sendProtected(PacketBuffer data) {
        try {
            sender(data);
            return true;
        } catch (IllegalArgumentException exc) {
            // Payload too large
            return false;
        }
    }

    public static boolean sendProtected(PacketBuffer data, EntityPlayerMP player) {
        try {
            sender(data, player);
            return true;
        } catch (IllegalArgumentException exc) {
            // Payload too large
            return false;
        }
    }

    public static void sender(PacketBuffer data) {
        for (EntityPlayerMP player : CarpetClientServer.getRegisteredPlayers()) {
            data.retain();
            PacketSplitter.send(player, CARPET_CHANNEL_NAME, data);
        }
        data.release();
    }

    public static void sender(PacketBuffer data, EntityPlayerMP player) {
        PacketSplitter.send(player, CARPET_CHANNEL_NAME, data);
    }

    public MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }
}
