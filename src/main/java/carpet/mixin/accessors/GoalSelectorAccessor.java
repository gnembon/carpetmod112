package carpet.mixin.accessors;

import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.entity.ai.goal.GoalSelector;

@Mixin(GoalSelector.class)
public interface GoalSelectorAccessor {
    @Accessor("field_33402") Set<AccessibleGoalSelectorEntry> getExecutingTaskEntries();
}
