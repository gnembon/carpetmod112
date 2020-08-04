package carpet.utils.extensions;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagList;

public interface BoundingBoxProvider {
    NBTTagList getBoundingBoxes(Entity entity);
}
