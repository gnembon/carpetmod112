package carpet.helpers;

import carpet.CarpetSettings;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.Map;

public class IronFarmOptimization {
    public static void recalculateDoorCache(VillageCollection villageCollection) {
        villageCollection.getDoorCache().clear();
        for (Village village : villageCollection.getVillageList()) {
            for (VillageDoorInfo doorInfo : village.getVillageDoorInfoList()) {
                addDoor(villageCollection, village, doorInfo.getDoorBlockPos());
            }
        }
    }

    public static VillageDoorInfo findDoorInfo(VillageCollection villageCollection, BlockPos doorBlock) {
        for (Village village : villageCollection.getDoorCache().get(doorBlock)) {
            int radius = village.getVillageRadius();
            BlockPos center = village.getCenter();
            if (center.distanceSq(doorBlock) <= radius * radius) {
                for (VillageDoorInfo doorInfo : village.getVillageDoorInfoList()) {
                    BlockPos blockPos = doorInfo.getDoorBlockPos();
                    if (blockPos.getX() == doorBlock.getX()
                            && blockPos.getZ() == doorBlock.getZ()
                            && Math.abs(blockPos.getY() - doorBlock.getY()) <= 1) {
                        return doorInfo;
                    }
                }
            }
        }
        return null;
    }

    public static void addDoor(VillageCollection villageCollection, Village village, BlockPos doorBlock) {
        ArrayListMultimap<BlockPos, Village> doorCache = villageCollection.getDoorCache();
        for (int i = -1; i <= 1; i++) {
            BlockPos blockPos = new BlockPos(doorBlock.getX(), doorBlock.getY() + i, doorBlock.getZ());
            doorCache.put(blockPos, village);
            doorCache.get(blockPos).sort(Comparator.comparingInt(v -> villageCollection.getVillageList().lastIndexOf(v)));
        }
    }

    public static void removeDoor(VillageCollection villageCollection, Village village, BlockPos blockPos) {
        for (int i = -1; i <= 1; i++) {
            BlockPos doorBlock = new BlockPos(blockPos.getX(), blockPos.getY() + i, blockPos.getZ());
            villageCollection.getDoorCache().remove(doorBlock, village);
        }
    }

    public static void onDoorRemoved(VillageCollection villageCollection, BlockPos doorBlock) {
        if (CarpetSettings.doorCheckOptimization) {
            if (CarpetSettings.doorSearchOptimization) {
                setDoorRemoved(villageCollection.getDoorCache().get(doorBlock), doorBlock);
            } else {
                setDoorRemoved(villageCollection.getVillageList(), doorBlock);
            }
        }
    }

    private static void setDoorRemoved(Iterable<Village> villages, BlockPos doorBlock) {
        for (Village village : villages) {
            for (VillageDoorInfo door : village.getVillageDoorInfoList()) {
                if (doorBlock.equals(door.getDoorBlockPos())) {
                    door.setLastActivityTimestamp(Integer.MIN_VALUE);
                }
            }
        }
    }

    public static void recalculateVillageChunks(VillageCollection villageCollection) {
        Map<ChunkPos, Integer> villageChunks = villageCollection.getVillageChunks();
        villageChunks.clear();
        for (Village village : villageCollection.getVillageList()) {
            for (VillageDoorInfo door : village.getVillageDoorInfoList()) {
                addChunk(villageChunks, door.getDoorBlockPos());
            }
        }
    }

    public static void addChunk(Map<ChunkPos, Integer> villageChunks, BlockPos doorBlock) {
        ChunkPos doorChunk = new ChunkPos(doorBlock);
        villageChunks.merge(doorChunk, 1, Integer::sum);
    }

    public static void removeChunk(Map<ChunkPos, Integer> villageChunks, BlockPos doorBlock) {
        ChunkPos doorChunk = new ChunkPos(doorBlock);
        int count = villageChunks.get(doorChunk) - 1;
        if (count <= 0) {
            villageChunks.remove(doorChunk);
        } else {
            villageChunks.put(doorChunk, count);
        }
    }

    public static int countEntitiesWithinAABB(World world, Class<? extends Entity> classEntity, AxisAlignedBB aabb, Map<AxisAlignedBB, Integer> entityCache) {
        Integer count = entityCache.get(aabb);
        if (count == null) {
            count = world.getEntitiesWithinAABB(classEntity, aabb).size();
            entityCache.put(aabb, count);
        }
        return count;
    }

    public static void updateAABBCache(AxisAlignedBB entity, Map<AxisAlignedBB, Integer> entityCache) {
        for (Map.Entry<AxisAlignedBB, Integer> entry : entityCache.entrySet()) {
            if (entry.getKey().intersects(entity)) {
                entry.setValue(entry.getValue() + 1);
            }
        }
    }
}
