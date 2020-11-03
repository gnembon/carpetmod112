package carpet.mixin.accessors;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor void setRidingEntity(Entity entity);
    @Accessor boolean isFirstUpdate();
    @Accessor int getFire();
    @Invoker void invokeSetRotation(float yaw, float pitch);
    @Invoker void invokeRemovePassenger(Entity passenger);
}
