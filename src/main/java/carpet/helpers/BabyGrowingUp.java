package carpet.helpers;
//Author: xcom

import java.util.List;

import carpet.mixin.accessors.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class BabyGrowingUp {

    public static void carpetSetSize(Entity entity, float width, float height) {
        float f = entity.width;
        entity.width = width;
        entity.height = height;
        Box oldAABB = entity.getBoundingBox();

        double d0 = (double) width / 2.0D;
        entity.setBoundingBox(new Box(entity.x - d0, entity.y, entity.z - d0, entity.x + d0,
                entity.y + (double) entity.height, entity.z + d0));

        if (entity.width > f && !((EntityAccessor) entity).isFirstUpdate() && !entity.world.isClient) {
            pushEntityOutOfBlocks(entity, oldAABB);
        }
    }

    private static void pushEntityOutOfBlocks(Entity entity, Box oldHitbox) {
        // Pass "null" in first argument to only get _possible_ block collisions
        List<Box> list1 = entity.world.getCollisionBoxes(null, entity.getBoundingBox());
        Box axisalignedbb = entity.getBoundingBox();

        for (Box aabb : list1) {
            if (!oldHitbox.intersects(aabb) && axisalignedbb.intersects(aabb)) {
                double minX = axisalignedbb.x1;
                double maxX = axisalignedbb.x2;
                double minZ = axisalignedbb.z1;
                double maxZ = axisalignedbb.z2;

                // Check for collisions on the X and Z axis, and only push the
                // new AABB if the colliding blocks AABB
                // is completely to the opposite side of the original AABB
                if (aabb.x2 > axisalignedbb.x1 && aabb.x1 < axisalignedbb.x2) {
                    if (aabb.x2 >= oldHitbox.x2 && aabb.x1 >= oldHitbox.x2) {
                        minX = aabb.x1 - entity.width;
                        maxX = aabb.x1;
                    } else if (aabb.x2 <= oldHitbox.x1 && aabb.x1 <= oldHitbox.x1) {
                        minX = aabb.x2;
                        maxX = aabb.x2 + entity.width;
                    }
                }

                if (aabb.z2 > axisalignedbb.z1 && aabb.z1 < axisalignedbb.z2) {
                    if (aabb.z1 >= oldHitbox.z2 && aabb.z2 >= oldHitbox.z2) {
                        minZ = aabb.z1 - entity.width;
                        maxZ = aabb.z1;
                    } else if (aabb.z2 <= oldHitbox.z1 && aabb.z1 <= oldHitbox.z1) {
                        minZ = aabb.z2;
                        maxZ = aabb.z2 + entity.width;
                    }
                }

                axisalignedbb = new Box(minX, axisalignedbb.y1, minZ, maxX, axisalignedbb.y2, maxZ);
            }
        }

        entity.setBoundingBox(axisalignedbb);
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
