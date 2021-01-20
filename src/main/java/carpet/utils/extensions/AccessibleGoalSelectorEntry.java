package carpet.utils.extensions;

import net.minecraft.entity.ai.goal.Goal;

public interface AccessibleGoalSelectorEntry {
    Goal getAction();
    int getPriority();
    boolean isUsing();
}
