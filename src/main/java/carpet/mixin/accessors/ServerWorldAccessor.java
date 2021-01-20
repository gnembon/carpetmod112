package carpet.mixin.accessors;

import net.minecraft.class_6380;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.TreeSet;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor("field_31701") class_6380 getPlayerChunkMap();
    @Accessor("field_31703") TreeSet<ScheduledTick> getPendingTickListEntriesTreeSet();
    @Accessor("field_31710") int getBlockEventCacheIndex();
    @Invoker("method_33463") BlockPos invokeAdjustPosToNearbyEntity(BlockPos pos);
}
