package carpet.helpers.lifetime.trackeddata;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;

public class ExperienceOrbTrackedData extends ExtraCountTrackedData
{
    @Override
    protected long getExtraCount(Entity entity)
    {
        return entity instanceof EntityXPOrb ? ((EntityXPOrb)entity).getXpValue() : 0L;
    }

    @Override
    protected String getCountDisplayString()
    {
        return "Experience Amount";
    }
}
