package carpet.mixin.accessors;

import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PistonBlock.class)
public interface PistonBlockAccessor {
    @Invoker boolean invokeShouldExtend(World world, BlockPos pos, Direction facing);
}
