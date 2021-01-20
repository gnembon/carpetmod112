package carpet.mixin.accessors;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("field_23571") void setScheduledUpdatesAreImmediate(boolean scheduledUpdatesAreImmediate);
    @Accessor("field_23579") int getUpdateLCG();
    @Accessor("field_23579") void setUpdateLCG(int seed);
    @Invoker("method_25980") boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty);
}
