package carpet.mixin.accessors;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.TreeSet;

@Mixin(WorldServer.class)
public interface WorldServerAccessor {
    @Accessor PlayerChunkMap getPlayerChunkMap();
    @Accessor TreeSet<NextTickListEntry> getPendingTickListEntriesTreeSet();
    @Accessor int getBlockEventCacheIndex();
    @Invoker BlockPos invokeAdjustPosToNearbyEntity(BlockPos pos);
}
