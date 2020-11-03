package carpet.mixin.accessors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(AnvilChunkLoader.class)
public interface AnvilChunkLoaderAccessor {
    @Accessor File getChunkSaveLocation();
    @Invoker void invokeWriteChunkToNBT(Chunk chunk, World world, NBTTagCompound tag);
}
