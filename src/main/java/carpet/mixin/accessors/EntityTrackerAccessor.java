package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.class_2509;
import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(class_2509.class)
public interface EntityTrackerAccessor {
    @Accessor("field_31685") Set<EntityTrackerEntry> getEntries();
}
