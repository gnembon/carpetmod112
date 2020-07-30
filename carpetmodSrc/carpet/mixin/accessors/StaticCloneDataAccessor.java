package carpet.mixin.accessors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.command.CommandClone$StaticCloneData")
public interface StaticCloneDataAccessor {
    @Accessor BlockPos getPos();
    @Accessor IBlockState getBlockState();
    @Accessor NBTTagCompound getNbt();
}
