package carpet.mixin.accessors;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockPistonBase.class)
public interface BlockPistonBaseAccessor {
    @Invoker boolean invokeShouldBeExtended(World world, BlockPos pos, EnumFacing facing);
}
