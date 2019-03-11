package carpet.carpetclient;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarpetClientRandomtickingIndexing {

    private static boolean[] updates = {false, false, false};
    private static boolean enableUpdates = false;
    private static List<EntityPlayerMP> players = new ArrayList<>();

    public static void enableUpdate(EntityPlayerMP player) {
        if (!enableUpdates) return;
        int dimention = player.world.provider.getDimensionType().getId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static boolean sendUpdates(World world) {
        int dimention = world.provider.getDimensionType().getId() + 1;
        return updates[dimention];
    }

    public static void register(EntityPlayerMP sender, PacketBuffer data) {
        boolean register = data.readBoolean();
        if (register) {
            registerPlayer(sender);
        } else {
            unregisterPlayer(sender);
        }
    }

    private static void registerPlayer(EntityPlayerMP sender) {
        players.add(sender);
        enableUpdates = true;
        int dimention = sender.world.provider.getDimensionType().getId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static void unregisterPlayer(EntityPlayerMP player) {
        players.remove(player);
        if (players.size() == 0) enableUpdates = false;
    }

    public static void sendRandomtickingChunkOrder(World world, PlayerChunkMap playerChunkMap) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        for (Iterator<Chunk> iterator = playerChunkMap.getChunkIterator(); iterator.hasNext(); ) {
            Chunk c = iterator.next();
            NBTTagCompound chunkData = new NBTTagCompound();
            chunkData.setInteger("x", c.x);
            chunkData.setInteger("z", c.z);
            nbttaglist.appendTag(chunkData);
        }
        compound.setTag("list", nbttaglist);
        for (EntityPlayerMP p : players) {
            CarpetClientMessageHandler.sendNBTRandomTickData(p, compound);
        }

        int dimention = world.provider.getDimensionType().getId() + 1;
        updates[dimention] = false;
    }

}
