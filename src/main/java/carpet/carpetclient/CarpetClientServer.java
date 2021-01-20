package carpet.carpetclient;

import java.util.LinkedHashSet;

import carpet.network.PacketSplitter;
import carpet.network.PluginChannelHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CarpetClientServer implements PluginChannelHandler {

    public static boolean activateInstantMine = true;
    private final MinecraftServer minecraftServer;
    private static final LinkedHashSet<ServerPlayerEntity> players = new LinkedHashSet<>();
    public static final String CARPET_CHANNEL_NAME = "carpet:client";
    public static final String MINE_CHANNEL_NAME = "carpet:mine";

    public CarpetClientServer(MinecraftServer server) {
        this.minecraftServer = server;
    }

    public String[] getChannels() {
        return new String[]{CARPET_CHANNEL_NAME, MINE_CHANNEL_NAME};
    }

    public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
        switch (packet.method_32939()) {
            case CARPET_CHANNEL_NAME:
                PacketByteBuf buffer = PacketSplitter.receive(player, packet);
                if(buffer != null) {
                    CarpetClientMessageHandler.handler(player, buffer);
                }
                break;
            case MINE_CHANNEL_NAME:
                // Mining packets for carpet client to get around few bugs and careful break. CARPET-XCOM
                PacketByteBuf payload = packet.method_32941();
                payload.readBoolean();
                boolean start = payload.readBoolean();
                BlockPos pos = payload.readBlockPos();
                Direction facing = Direction.byId(payload.readUnsignedByte());
                activateInstantMine = payload.readBoolean();
                if (start) {
                    if (!this.minecraftServer.isSpawnProtected(player.world, pos, player) && player.world.getWorldBorder().contains(pos)) {
                        player.interactionManager.processBlockBreakingAction(pos, facing);
                    } else {
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.world, pos));
                    }
                } else {
                    player.interactionManager.method_33535(pos);
                }
                activateInstantMine = true;
                break;
        }
    }

    public boolean register(String channel, ServerPlayerEntity sender) {
        players.add(sender);
        CarpetClientMessageHandler.sendAllGUIOptions(sender);
        CarpetClientMessageHandler.sendCustomRecipes(sender);
        return true;
    }

    public void unregister(String channel, ServerPlayerEntity player) {
        players.remove(player);
        CarpetClientMarkers.unregisterPlayerVillageMarkers(player);
        CarpetClientChunkLogger.logger.unregisterPlayer(player);
        CarpetClientRandomtickingIndexing.unregisterPlayer(player);
    }

    static public LinkedHashSet<ServerPlayerEntity> getRegisteredPlayers() {
        return players;
    }

    public static boolean isPlayerRegistered(ServerPlayerEntity player) {
        return players.contains(player);
    }

    public static boolean sendProtected(PacketByteBuf data) {
        try {
            sender(data);
            return true;
        } catch (IllegalArgumentException exc) {
            // Payload too large
            return false;
        }
    }

    public static boolean sendProtected(PacketByteBuf data, ServerPlayerEntity player) {
        try {
            sender(data, player);
            return true;
        } catch (IllegalArgumentException exc) {
            // Payload too large
            return false;
        }
    }

    public static void sender(PacketByteBuf data) {
        for (ServerPlayerEntity player : CarpetClientServer.getRegisteredPlayers()) {
            data.retain();
            PacketSplitter.send(player, CARPET_CHANNEL_NAME, data);
        }
        data.release();
    }

    public static void sender(PacketByteBuf data, ServerPlayerEntity player) {
        PacketSplitter.send(player, CARPET_CHANNEL_NAME, data);
    }

    public MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }
}
