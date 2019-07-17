package carpet.carpetclient;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CarpetClientMarkers {

    public static final int OUTER_BOUNDING_BOX = 0;
    public static final int END_CITY = 1;
    public static final int FORTRESS = 2;
    public static final int TEMPLE = 3;
    public static final int VILLAGE = 4;
    public static final int STRONGHOLD = 5;
    public static final int MINESHAFT = 6;
    public static final int MONUMENT = 7;
    public static final int MANSION = 8;

    private static ArrayList<EntityPlayerMP> playersVillageMarkers = new ArrayList<>();

    public static void updateClientVillageMarkers(World worldObj) {
        if (playersVillageMarkers.size() == 0) {
            return;
        }
        NBTTagList nbttaglist = new NBTTagList();
        NBTTagCompound tagCompound = new NBTTagCompound();

        for (Village village : worldObj.getVillageCollection().getVillageList()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            village.writeVillageDataToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }

        tagCompound.setTag("Villages", nbttaglist);

        for (EntityPlayerMP sender : playersVillageMarkers) {
            CarpetClientMessageHandler.sendNBTVillageData(sender, tagCompound);
        }
    }

    public static void updateClientBoundingBoxMarkers(EntityPlayerMP sender, PacketBuffer data) {
        MinecraftServer ms = sender.world.getMinecraftServer();
        WorldServer ws = ms.getWorld(sender.dimension);
        NBTTagList list = ws.getChunkProvider().getBoundingBoxes(sender);
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.setTag("Boxes", list);
        nbttagcompound.setInteger("Dimention", sender.dimension);
        nbttagcompound.setLong("Seed", sender.world.getSeed());

        CarpetClientMessageHandler.sendNBTBoundingboxData(sender, nbttagcompound);
    }

    public static void registerVillagerMarkers(EntityPlayerMP sender, PacketBuffer data) {
        boolean addPlayer = data.readBoolean();
        if (addPlayer) {
            playersVillageMarkers.add(sender);
            updateClientVillageMarkers(sender.world);
        } else {
            playersVillageMarkers.remove(sender);
        }
    }

    public static void unregisterPlayerVillageMarkers(EntityPlayerMP player) {
        playersVillageMarkers.remove(player);
    }
}
