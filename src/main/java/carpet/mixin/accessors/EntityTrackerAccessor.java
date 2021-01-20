package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.entity.EntityTracker;
import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor("field_31685") Set<EntityTrackerEntry> getEntries();
}
