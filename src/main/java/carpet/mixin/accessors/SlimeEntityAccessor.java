package carpet.mixin.accessors;

import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SlimeEntity.class)
public interface SlimeEntityAccessor {
    @Invoker int invokeGetDamageAmount();
}
