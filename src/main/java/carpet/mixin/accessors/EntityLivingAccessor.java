package carpet.mixin.accessors;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLiving.class)
public interface EntityLivingAccessor {
    @Accessor EntityAITasks getTasks();
    @Accessor boolean getPersistenceRequired();
    @Invoker boolean invokeCanDespawn();
}
