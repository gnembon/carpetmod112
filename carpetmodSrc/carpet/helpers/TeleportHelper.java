package carpet.helpers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class TeleportHelper {

    public static void changeDimensions(EntityPlayerMP player, EntityPlayerMP target){
        // Adapted from spectator teleport code (NetHandlerPlayServer::handleSpectate)
        double x = target.posX;
        double y = target.posY;
        double z = target.posZ;
        MinecraftServer server = player.getServer();
        assert server != null;

        WorldServer worldFrom = (WorldServer) player.world;
        WorldServer worldTo = (WorldServer) target.world;
        int dimension = worldTo.provider.getDimensionType().getId();
        player.dimension = dimension;

        player.connection.sendPacket(new SPacketRespawn(dimension, worldFrom.getDifficulty(), worldFrom.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
        server.getPlayerList().updatePermissionLevel(player);
        worldFrom.removeEntityDangerously(player);
        player.isDead = false;
        player.setLocationAndAngles(x, y, z, (float) target.rotationYaw, (float) target.rotationPitch);

        worldFrom.updateEntityWithOptionalForce(player, false);
        worldTo.spawnEntity(player);
        worldTo.updateEntityWithOptionalForce(player, false);

        player.setWorld(worldTo);
        server.getPlayerList().preparePlayer(player, worldFrom);

        player.setPositionAndUpdate(x, y, z);
        player.interactionManager.setWorld(worldTo);
        server.getPlayerList().updateTimeAndWeatherForPlayer(player, worldTo);
        server.getPlayerList().syncPlayerInventory(player);
    }
}
