package carpet.mixin.accessors;

import net.minecraft.entity.monster.EntitySlime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntitySlime.class)
public interface EntitySlimeAccessor {
    @Invoker int invokeGetAttackStrength();
}
