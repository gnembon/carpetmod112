package carpet.mixin.accessors;

import carpet.utils.extensions.AccessibleAITaskEntry;
import net.minecraft.entity.ai.EntityAIBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.entity.ai.EntityAITasks$EntityAITaskEntry")
public class EntityAITaskEntryAccessor implements AccessibleAITaskEntry {
    @Shadow @Final public EntityAIBase action;
    @Shadow @Final public int priority;
    @Shadow public boolean using;

    @Override
    public EntityAIBase getAction() {
        return action;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isUsing() {
        return using;
    }
}
