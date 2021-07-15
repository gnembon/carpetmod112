package carpet.helpers.lifetime.trackeddata;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

public class ItemTrackedData extends ExtraCountTrackedData
{
    @Override
    protected long getExtraCount(Entity entity)
    {
        return entity instanceof EntityItem ? ((EntityItem)entity).getItem().getCount() : 0L;
    }

    @Override
    protected String getCountDisplayString()
    {
        return "Item Count";
    }
}
