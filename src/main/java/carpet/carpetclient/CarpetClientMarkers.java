package carpet.carpetclient;

import java.util.ArrayList;

import carpet.mixin.accessors.StructureFeatureAccessor;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.class_2792;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;

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

    private static ArrayList<ServerPlayerEntity> playersVillageMarkers = new ArrayList<>();

    public static void updateClientVillageMarkers(World worldObj) {
        if (playersVillageMarkers.size() == 0) {
            return;
        }
        ListTag nbttaglist = new ListTag();
        CompoundTag tagCompound = new CompoundTag();

        for (class_2792 village : worldObj.method_26061().method_35119()) {
            CompoundTag nbttagcompound = new CompoundTag();
            village.method_35090(nbttagcompound);
            nbttaglist.add(nbttagcompound);
        }

        tagCompound.put("Villages", nbttaglist);

        for (ServerPlayerEntity sender : playersVillageMarkers) {
            CarpetClientMessageHandler.sendNBTVillageData(sender, tagCompound);
        }
    }

    public static void updateClientBoundingBoxMarkers(ServerPlayerEntity sender, PacketByteBuf data) {
        MinecraftServer ms = sender.world.getServer();
        ServerWorld ws = ms.getWorldById(sender.field_33045);
        ListTag list = ((BoundingBoxProvider) ws.getChunkManager()).getBoundingBoxes(sender);
        CompoundTag nbttagcompound = new CompoundTag();

        nbttagcompound.put("Boxes", list);
        nbttagcompound.putInt("Dimention", sender.field_33045);
        nbttagcompound.putLong("Seed", sender.world.getSeed());

        CarpetClientMessageHandler.sendNBTBoundingboxData(sender, nbttagcompound);
    }

    public static void registerVillagerMarkers(ServerPlayerEntity sender, PacketByteBuf data) {
        boolean addPlayer = data.readBoolean();
        if (addPlayer) {
            playersVillageMarkers.add(sender);
            updateClientVillageMarkers(sender.world);
        } else {
            playersVillageMarkers.remove(sender);
        }
    }

    public static void unregisterPlayerVillageMarkers(ServerPlayerEntity player) {
        playersVillageMarkers.remove(player);
    }

    // Retrieval method to get the bounding boxes CARPET-XCOM
    public static ListTag getBoundingBoxes(StructureFeature structure, Entity entity, int type) {
        ListTag list = new ListTag();
        for (StructureStart structurestart : ((StructureFeatureAccessor) structure).getStructureMap().values()) {
            if (MathHelper.sqrt(new ColumnPos(structurestart.method_27902(), structurestart.method_27903()).method_25893(entity)) > 700) {
                continue;
            }
            CompoundTag outerBox = new CompoundTag();
            outerBox.putInt("type", OUTER_BOUNDING_BOX);
            outerBox.put("bb", structurestart.getBoundingBox().toNbt());
            list.add(outerBox);
            for (StructurePiece child : structurestart.getChildren()) {
                CompoundTag innerBox = new CompoundTag();
                innerBox.putInt("type", type);
                innerBox.put("bb", child.getBoundingBox().toNbt());
                list.add(innerBox);
            }
        }
        return list;
    }
}
