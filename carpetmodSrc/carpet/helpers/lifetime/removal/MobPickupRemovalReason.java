package carpet.helpers.lifetime.removal;

import carpet.helpers.lifetime.utils.LifeTimeTrackerUtil;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

// for item entity and xp orb entity
public class MobPickupRemovalReason extends RemovalReason
{
    private final Class<? extends Entity> pickerType;

    public MobPickupRemovalReason(Class<? extends Entity> pickerType)
    {
        this.pickerType = pickerType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobPickupRemovalReason that = (MobPickupRemovalReason) o;
        return Objects.equals(pickerType, that.pickerType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pickerType);
    }

    @Override
    public ITextComponent toText()
    {
        return Messenger.c("w Picked up by " + LifeTimeTrackerUtil.getEntityTypeDescriptor(this.pickerType));
    }
}
