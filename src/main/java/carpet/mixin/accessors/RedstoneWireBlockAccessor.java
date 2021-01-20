package carpet.mixin.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneWireBlockAccessor {
    @Accessor boolean getWiresGivePower();
    @Accessor void setWiresGivePower(boolean wiresGivePower);
    @Invoker boolean invokeCouldConnectTo(BlockView world, BlockPos pos, Direction side);
    @Invoker("method_26762") BlockState invokeCalculateCurrentChanges(World world, BlockPos pos1, BlockPos pos2, BlockState state);
}
