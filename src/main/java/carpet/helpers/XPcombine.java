package carpet.helpers;
//Author: xcom

import carpet.mixin.accessors.ExperienceOrbEntityAccessor;
import carpet.utils.extensions.ExtendedExperienceOrbEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;

public class XPcombine {

    public static void searchForOtherXPNearbyCarpet(ExperienceOrbEntity first)
    {
        for (ExperienceOrbEntity entityxp : first.world.getEntities(ExperienceOrbEntity.class,
                first.getBoundingBox().stretch(0.5D, 0.0D, 0.5D)))
        {
            combineItems(first, entityxp);
        }
    }

    private static boolean combineItems(ExperienceOrbEntity first, ExperienceOrbEntity other)
    {
        if (other == first)
        {
            return false;
        }
        else if (other.isAlive() && first.isAlive())
        {
            if (first.pickupDelay != 32767 && other.pickupDelay != 32767)
            {
                if (first.orbAge != -32768 && other.orbAge != -32768
                        && ((ExtendedExperienceOrbEntity) first).getDelayBeforeCombine() == 0 && ((ExtendedExperienceOrbEntity) other).getDelayBeforeCombine() == 0)
                {
                    int size = getTextureByXP(other.getExperienceAmount() );
                    ((ExperienceOrbEntityAccessor) other).setAmount(other.getExperienceAmount() + first.getExperienceAmount());
                    other.pickupDelay = Math.max(other.pickupDelay, first.pickupDelay);
                    other.orbAge = Math.min(other.orbAge, first.orbAge);
                    if (getTextureByXP(other.getExperienceAmount() ) != size)
                    {
                        other.remove();
                        first.world.spawnEntity(newXPOrb(other.world, other.getExperienceAmount(), other));
                    }
                    else
                    {
                        ((ExtendedExperienceOrbEntity) other).setDelayBeforeCombine(50);
                    }
                    first.remove();
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

    private static ExperienceOrbEntity newXPOrb(World world, int expValue, ExperienceOrbEntity old) {
        ExperienceOrbEntity orb = new ExperienceOrbEntity(world, old.x, old.y, old.z, expValue);
        orb.yaw = old.yaw;
        orb.velocityX = old.velocityX;
        orb.velocityY = old.velocityY;
        orb.velocityZ = old.velocityZ;
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
