package carpet.utils.extensions;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListTag;

public interface BoundingBoxProvider {
    ListTag getBoundingBoxes(Entity entity);
}
