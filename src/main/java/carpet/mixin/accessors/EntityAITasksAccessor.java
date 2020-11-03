package carpet.mixin.accessors;

import carpet.utils.extensions.AccessibleAITaskEntry;
import net.minecraft.entity.ai.EntityAITasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(EntityAITasks.class)
public interface EntityAITasksAccessor {
    @Accessor Set<AccessibleAITaskEntry> getExecutingTaskEntries();
}
