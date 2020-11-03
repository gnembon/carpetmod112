package carpet.mixin.accessors;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor Set<EntityTrackerEntry> getEntries();
}
