package carpet.helpers.lifetime.spawning;

import carpet.helpers.lifetime.utils.LifeTimeTrackerUtil;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

// for item entity and xp orb entity
public class MobDropSpawningReason extends SpawningReason
{
    private final Class<? extends Entity> providerType;

    public MobDropSpawningReason(Class<? extends Entity> providerType)
    {
        this.providerType = providerType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobDropSpawningReason that = (MobDropSpawningReason) o;
        return Objects.equals(providerType, that.providerType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(providerType);
    }

    @Override
    public ITextComponent toText()
    {
        return Messenger.c("w Dropped by " + LifeTimeTrackerUtil.getEntityTypeDescriptor(this.providerType));
    }
}
