package carpet.mixin.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.command.CloneCommand$class_5925")
public interface StaticCloneDataAccessor {
    @Accessor("field_29493") BlockPos getPos();
    @Accessor("field_29494") BlockState getBlockState();
    @Accessor("field_29495") CompoundTag getNbt();
}
