package carpet.helpers.lifetime.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class LifeTimeTrackerUtil
{
    public static boolean isTrackedEntity(Entity entity)
    {
        return entity instanceof EntityLiving || entity instanceof EntityItem || entity instanceof EntityXPOrb;
    }

    public static String getEntityTypeDescriptor(Class<? extends Entity> entityType)
    {
        if (EntityPlayer.class.isAssignableFrom(entityType))
        {
            return "player";
        }
        ResourceLocation resourceLocation = EntityList.REGISTRY.getNameForObject(entityType);
        return resourceLocation != null ? resourceLocation.getPath() : entityType.getSimpleName();
    }

    public static Optional<Class<? extends Entity>> getEntityTypeFromName(String name)
    {
        ResourceLocation resourcelocation = new ResourceLocation(name);
        return Optional.ofNullable(EntityList.REGISTRY.getObject(resourcelocation));
    }
}
