package carpet.mixin.accessors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageAccessor {
    @Accessor("field_25418") File getChunkSaveLocation();
    @Invoker("method_27467") void invokeWriteChunkToNBT(Chunk chunk, World world, CompoundTag tag);
}
