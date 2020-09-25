package carpet.mixin.accessors;

import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityXPOrb.class)
public interface EntityXPOrbAccessor {
    @Accessor void setXpValue(int value);
}
