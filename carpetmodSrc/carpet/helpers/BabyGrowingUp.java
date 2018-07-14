package carpet.helpers;
//Author: xcom

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class BabyGrowingUp {

    public static void carpetSetSize(Entity entity, float width, float height) {
        float f = entity.width;
        entity.width = width;
        entity.height = height;
        AxisAlignedBB oldAABB = entity.getEntityBoundingBox();

        double d0 = (double) width / 2.0D;
        entity.setEntityBoundingBox(new AxisAlignedBB(entity.posX - d0, entity.posY, entity.posZ - d0, entity.posX + d0,
                entity.posY + (double) entity.height, entity.posZ + d0));

        if (entity.width > f && !entity.firstUpdate && !entity.world.isRemote) {
            pushEntityOutOfBlocks(entity, oldAABB);
        }
    }

    private static void pushEntityOutOfBlocks(Entity entity, AxisAlignedBB oldHitbox) {
        // Pass "null" in first argument to only get _possible_ block collisions
        List<AxisAlignedBB> list1 = entity.world.getCollisionBoxes(null, entity.getEntityBoundingBox());
        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();

        for (AxisAlignedBB aabb : list1) {
            if (!oldHitbox.intersects(aabb) && axisalignedbb.intersects(aabb)) {
                double minX = axisalignedbb.minX;
                double maxX = axisalignedbb.maxX;
                double minZ = axisalignedbb.minZ;
                double maxZ = axisalignedbb.maxZ;

                // Check for collisions on the X and Z axis, and only push the
                // new AABB if the colliding blocks AABB
                // is completely to the opposite side of the original AABB
                if (aabb.maxX > axisalignedbb.minX && aabb.minX < axisalignedbb.maxX) {
                    if (aabb.maxX >= oldHitbox.maxX && aabb.minX >= oldHitbox.maxX) {
                        minX = aabb.minX - entity.width;
                        maxX = aabb.minX;
                    } else if (aabb.maxX <= oldHitbox.minX && aabb.minX <= oldHitbox.minX) {
                        minX = aabb.maxX;
                        maxX = aabb.maxX + entity.width;
                    }
                }

                if (aabb.maxZ > axisalignedbb.minZ && aabb.minZ < axisalignedbb.maxZ) {
                    if (aabb.minZ >= oldHitbox.maxZ && aabb.maxZ >= oldHitbox.maxZ) {
                        minZ = aabb.minZ - entity.width;
                        maxZ = aabb.minZ;
                    } else if (aabb.maxZ <= oldHitbox.minZ && aabb.minZ <= oldHitbox.minZ) {
                        minZ = aabb.maxZ;
                        maxZ = aabb.maxZ + entity.width;
                    }
                }

                axisalignedbb = new AxisAlignedBB(minX, axisalignedbb.minY, minZ, maxX, axisalignedbb.maxY, maxZ);
            }
        }

        entity.setEntityBoundingBox(axisalignedbb);
    }

    // public static void pushEntityOutOfBlocks(Entity entity, AxisAlignedBB
    // oldHitbox) {
    // List<AxisAlignedBB> list1 = entity.world.getCollisionBoxes(entity,
    // entity.getEntityBoundingBox());
    // AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
    //
    // for (AxisAlignedBB i : list1) {
    // double x = calculateX(axisalignedbb, i);
    // double z = calculateZ(axisalignedbb, i);
    //
    // if (!oldHitbox.intersectsWith(i))
    // entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(x, 0.0D,
    // z));
    //
    // axisalignedbb = entity.getEntityBoundingBox();
    // }
    // }
    //
    // public static double calculateX(AxisAlignedBB base, AxisAlignedBB box) {
    // if (box.maxY > base.minY && box.minY < base.maxY && box.maxZ > base.minZ
    // && box.minZ < base.maxZ) {
    // double x = (base.maxX + base.minX) / 2.0D;
    //
    // if (x < box.minX) {
    // return box.minX - base.maxX;
    // } else if (x > box.maxX) {
    // return box.maxX - base.minX;
    // }
    // }
    //
    // return 0;
    // }
    //
    // public static double calculateZ(AxisAlignedBB base, AxisAlignedBB box) {
    // if (box.maxX > base.minX && box.minX < base.maxX && box.maxY > base.minY
    // && box.minY < base.maxY) {
    // double z = (base.maxZ + base.minZ) / 2.0D;
    //
    // if (z < box.minZ) {
    // return box.minZ - base.maxZ;
    // } else if (z > box.maxZ) {
    // return box.maxZ - base.minZ;
    // }
    // }
    //
    // return 0;
    // }
}
