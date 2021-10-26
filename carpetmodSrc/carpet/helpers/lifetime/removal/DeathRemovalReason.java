package carpet.helpers.lifetime.removal;

import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class DeathRemovalReason extends RemovalReason
{
    private final String damageSourceName;

    public DeathRemovalReason(DamageSource damageSource)
    {
        this.damageSourceName = damageSource.getDamageType();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof DeathRemovalReason)) return false;
        DeathRemovalReason that = (DeathRemovalReason) o;
        return Objects.equals(this.damageSourceName, that.damageSourceName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.damageSourceName);
    }

    @Override
    public ITextComponent toText()
    {
        return Messenger.c(
                "w Death",
                "g  (",
                TextUtil.getFancyText(
                        null,
                        Messenger.s(null, this.damageSourceName),
                        Messenger.s(null, "Damage source"),
                        null
                ),
                "g )"
        );
    }
}
