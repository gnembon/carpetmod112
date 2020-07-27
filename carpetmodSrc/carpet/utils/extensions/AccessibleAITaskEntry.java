package carpet.utils.extensions;

import net.minecraft.entity.ai.EntityAIBase;

public interface AccessibleAITaskEntry {
    EntityAIBase getAction();
    int getPriority();
    boolean isUsing();
}
