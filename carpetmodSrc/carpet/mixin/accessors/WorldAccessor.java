package carpet.mixin.accessors;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor void setScheduledUpdatesAreImmediate(boolean scheduledUpdatesAreImmediate);
    @Accessor int getUpdateLCG();
    @Accessor void setUpdateLCG(int seed);
    @Invoker boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty);
}
