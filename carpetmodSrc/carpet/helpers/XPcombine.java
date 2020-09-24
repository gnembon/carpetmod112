package carpet.helpers;
//Author: xcom

import carpet.mixin.accessors.EntityXPOrbAccessor;
import carpet.utils.extensions.ExtendedEntityXPOrb;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.world.World;

public class XPcombine {

    public static void searchForOtherXPNearbyCarpet(EntityXPOrb first)
    {
        for (EntityXPOrb entityxp : first.world.getEntitiesWithinAABB(EntityXPOrb.class,
                first.getEntityBoundingBox().expand(0.5D, 0.0D, 0.5D)))
        {
            combineItems(first, entityxp);
        }
    }

    private static boolean combineItems(EntityXPOrb first, EntityXPOrb other)
    {
        if (other == first)
        {
            return false;
        }
        else if (other.isEntityAlive() && first.isEntityAlive())
        {
            if (first.delayBeforeCanPickup != 32767 && other.delayBeforeCanPickup != 32767)
            {
                if (first.xpOrbAge != -32768 && other.xpOrbAge != -32768
                        && ((ExtendedEntityXPOrb) first).getDelayBeforeCombine() == 0 && ((ExtendedEntityXPOrb) other).getDelayBeforeCombine() == 0)
                {
                    int size = getTextureByXP(other.getXpValue() );
                    ((EntityXPOrbAccessor) other).setXpValue(other.getXpValue() + first.getXpValue());
                    other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, first.delayBeforeCanPickup);
                    other.xpOrbAge = Math.min(other.xpOrbAge, first.xpOrbAge);
                    if (getTextureByXP(other.getXpValue() ) != size)
                    {
                        other.setDead();
                        first.world.spawnEntity(newXPOrb(other.world, other.getXpValue(), other));
                    }
                    else
                    {
                        ((ExtendedEntityXPOrb) other).setDelayBeforeCombine(50);
                    }
                    first.setDead();
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private static EntityXPOrb newXPOrb(World world, int expValue, EntityXPOrb old) {
        EntityXPOrb orb = new EntityXPOrb(world, old.posX, old.posY, old.posZ, expValue);
        orb.rotationYaw = old.rotationYaw;
        orb.motionX = old.motionX;
        orb.motionY = old.motionY;
        orb.motionZ = old.motionZ;
        return orb;
    }

    public static int getTextureByXP(int value)
    {
        if (value >= 2477)
        {
            return 10;
        }
        else if (value >= 1237)
        {
            return 9;
        }
        else if (value >= 617)
        {
            return 8;
        }
        else if (value >= 307)
        {
            return 7;
        }
        else if (value >= 149)
        {
            return 6;
        }
        else if (value >= 73)
        {
            return 5;
        }
        else if (value >= 37)
        {
            return 4;
        }
        else if (value >= 17)
        {
            return 3;
        }
        else if (value >= 7)
        {
            return 2;
        }
        else
        {
            return value >= 3 ? 1 : 0;
        }
    }
}
