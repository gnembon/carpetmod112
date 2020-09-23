package carpet.mixin.accessors;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockRedstoneWire.class)
public interface BlockRedstoneWireAccessor {
    @Accessor boolean getCanProvidePower();
    @Accessor void setCanProvidePower(boolean canProvidePower);
    @Invoker boolean invokeIsPowerSourceAt(IBlockAccess world, BlockPos pos, EnumFacing side);
    @Invoker IBlockState invokeCalculateCurrentChanges(World world, BlockPos pos1, BlockPos pos2, IBlockState state);
}
