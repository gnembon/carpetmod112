package carpet.utils.extensions;

import carpet.carpetclient.ChunkLoadingReason;
import net.minecraft.entity.Entity;

public interface ExtendedAITask extends ChunkLoadingReason {
    Entity getEntity();
    String getTask();

    default String getDescription() {
        return "Entity: " + getEntity().getName() + ", Task: " + getTask();
    }
}
