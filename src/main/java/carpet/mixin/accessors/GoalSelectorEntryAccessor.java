package carpet.mixin.accessors;

import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import net.minecraft.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// TODO: make this an access widener
@Mixin(targets = "net.minecraft.entity.ai.goal.GoalSelector$class_6475")
public class GoalSelectorEntryAccessor implements AccessibleGoalSelectorEntry {
    @Shadow @Final public Goal field_33407;
    @Shadow @Final public int field_33408;
    @Shadow public boolean field_33409;

    @Override
    public Goal getAction() {
        return field_33407;
    }

    @Override
    public int getPriority() {
        return field_33408;
    }

    @Override
    public boolean isUsing() {
        return field_33409;
    }
}
