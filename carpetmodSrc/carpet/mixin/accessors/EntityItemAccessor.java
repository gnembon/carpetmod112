package carpet.mixin.accessors;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityItem.class)
public interface EntityItemAccessor {
    @Accessor int getAge();
    @Accessor void setAge(int age);
    @Accessor int getPickupDelay();
    @Accessor void setPickupDelay(int pickupDelay);
}
